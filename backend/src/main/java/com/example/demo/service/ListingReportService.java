package com.example.demo.service;

import com.example.demo.model.ListingReport;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.ListingReportRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ListingReportService {

    private final ListingReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ListingReport submitReport(Long anuntId, Long userId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Motivul raportului este obligatoriu.");
        }
        if (reason.length() > 2000) {
            throw new IllegalArgumentException("Motivul este prea lung (maxim 2000 caractere).");
        }

        Product product = productRepository.findById(anuntId)
                .orElseThrow(() -> new IllegalArgumentException("Anunț inexistent."));
        if (product.getUserId() != null && product.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Nu poți raporta propriul anunț.");
        }
        if (reportRepository.existsByUserIdAndAnuntIdAndStatus(userId, anuntId, "OPEN")) {
            throw new IllegalArgumentException("Ai deja un raport deschis pentru acest anunț.");
        }

        ListingReport report = new ListingReport();
        report.setUserId(userId);
        report.setAnuntId(anuntId);
        report.setReason(reason.trim());
        report.setStatus("OPEN");
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listForAdmin(String status) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        List<ListingReport> reports = status == null || status.isBlank()
                ? reportRepository.findAll().stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList()
                : reportRepository.findByStatusOrderByCreatedAtDesc(status);

        return reports.stream().map(this::toAdminRow).toList();
    }

    @Transactional
    public ListingReport dismiss(Long id) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        ListingReport report = reportRepository.findById(id).orElseThrow();
        report.setStatus("DISMISSED");
        return reportRepository.save(report);
    }

    @Transactional
    public ListingReport markReviewed(Long id) {
        SecurityUtils.requireRole(UserRole.MODERATOR);
        ListingReport report = reportRepository.findById(id).orElseThrow();
        report.setStatus("REVIEWED");
        return reportRepository.save(report);
    }

    public long countOpen() {
        return reportRepository.countByStatus("OPEN");
    }

    private Map<String, Object> toAdminRow(ListingReport report) {
        User reporter = userRepository.findById(report.getUserId()).orElse(null);
        Product listing = productRepository.findById(report.getAnuntId()).orElse(null);

        Map<String, Object> row = new HashMap<>();
        row.put("id", report.getId());
        row.put("userId", report.getUserId());
        row.put("reporterName", reporter != null ? reporter.getNume() : "Utilizator");
        row.put("reporterEmail", reporter != null ? reporter.getEmail() : null);
        row.put("anuntId", report.getAnuntId());
        row.put("listingTitle", listing != null ? listing.getTitlu() : "Anunț #" + report.getAnuntId());
        row.put("reason", report.getReason());
        row.put("status", report.getStatus());
        row.put("createdAt", report.getCreatedAt());
        return row;
    }
}
