package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingService {

    private static final Pattern LAT = Pattern.compile("\"lat\"\\s*:\\s*\"([\\d.\\-]+)\"");
    private static final Pattern LON = Pattern.compile("\"lon\"\\s*:\\s*\"([\\d.\\-]+)\"");

    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<double[]> geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", address + ", Romania")
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            if (json == null || json.equals("[]")) {
                return Optional.empty();
            }
            Matcher latM = LAT.matcher(json);
            Matcher lonM = LON.matcher(json);
            if (!latM.find() || !lonM.find()) {
                return Optional.empty();
            }
            return Optional.of(new double[]{
                    Double.parseDouble(latM.group(1)),
                    Double.parseDouble(lonM.group(1))
            });
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
