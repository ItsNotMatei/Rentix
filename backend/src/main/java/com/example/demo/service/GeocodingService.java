package com.example.demo.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingService {

    private static final String USER_AGENT = "RentixApp/1.0 (contact@rentix.test)";
    private static final Pattern LAT = Pattern.compile("\"lat\"\\s*:\\s*\"([\\d.\\-]+)\"");
    private static final Pattern LON = Pattern.compile("\"lon\"\\s*:\\s*\"([\\d.\\-]+)\"");

    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<double[]> geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        String normalized = appendRomaniaIfMissing(address.trim());
        try {
            String url = UriComponentsBuilder
                    .fromUriString("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", normalized)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("countrycodes", "ro")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Accept-Language", "ro");
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            String json = response.getBody();
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

    private String appendRomaniaIfMissing(String address) {
        String lower = address.toLowerCase();
        if (!lower.contains("romania") && !lower.contains("românia")) {
            return address + ", Romania";
        }
        return address;
    }
}
