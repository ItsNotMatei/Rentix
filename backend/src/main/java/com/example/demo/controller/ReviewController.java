package com.example.demo.controller;

import com.example.demo.security.SecurityUtils;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body
    ) {
        Integer rating = body.get("rating") instanceof Number n ? n.intValue() : null;
        String comment = body.get("comment") != null ? body.get("comment").toString() : "";
        return ResponseEntity.ok(
                reviewService.addReview(productId, SecurityUtils.currentUserId(), rating, comment)
        );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewDtos(productId));
    }

    @GetMapping("/{productId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewStats(productId));
    }

    @GetMapping("/{productId}/can-review")
    public ResponseEntity<?> canReview(@PathVariable Long productId) {
        Long userId = SecurityUtils.currentUserId();
        boolean can = reviewService.canReviewProduct(userId, productId);
        return ResponseEntity.ok(Map.of("canReview", can));
    }
}
