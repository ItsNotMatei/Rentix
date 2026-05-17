package com.example.demo.controller;

import com.example.demo.model.ModerationReport;
import com.example.demo.model.User;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return adminService.getStats();
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return adminService.analytics();
    }

    @GetMapping("/users")
    public Page<User> users(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminService.searchUsers(q, page, size);
    }

    @PatchMapping("/users/{id}/ban")
    public User ban(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return adminService.banUser(id, body.getOrDefault("reason", "Încălcare reguli"));
    }

    @PatchMapping("/users/{id}/suspend")
    public User suspend(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 7;
        String reason = body.getOrDefault("reason", "Suspendare temporară").toString();
        return adminService.suspendUser(id, days, reason);
    }

    @PatchMapping("/users/{id}/unban")
    public User unban(@PathVariable Long id) {
        return adminService.unbanUser(id);
    }

    @PatchMapping("/users/{id}/role")
    public User role(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return adminService.updateRole(id, body.get("role"));
    }

    @GetMapping("/listings")
    public Page<com.example.demo.model.Product> listings(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminService.listingsPage(page, size, q);
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        adminService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviews")
    public org.springframework.data.domain.Page<com.example.demo.model.Review> reviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminService.reviewsPage(page, size);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    public ResponseEntity<?> reports(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getReports(status));
    }

    @PatchMapping("/reports/{id}/resolve")
    public ModerationReport resolveReport(@PathVariable Long id) {
        return adminService.resolveReport(id);
    }

    @PostMapping("/reports")
    public ModerationReport createReport(@RequestBody Map<String, Object> body) {
        return adminService.createReport(
                body.get("type").toString(),
                Long.valueOf(body.get("targetId").toString()),
                body.getOrDefault("reason", "").toString(),
                SecurityUtils.currentUserId()
        );
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> bookings() {
        return ResponseEntity.ok(adminService.allReservations());
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> conversations() {
        return ResponseEntity.ok(adminService.allConversations());
    }
}
