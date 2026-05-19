package com.example.demo.controller;

import com.example.demo.model.ListingReport;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.ListingReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ListingReportController {

    private final ListingReportService listingReportService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Long anuntId = Long.valueOf(body.get("anuntId").toString());
        String reason = body.getOrDefault("reason", "").toString();
        ListingReport saved = listingReportService.submitReport(
                anuntId,
                SecurityUtils.currentUserId(),
                reason
        );
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "message", "Raportul a fost trimis. Echipa Rentix îl va analiza."
        ));
    }
}
