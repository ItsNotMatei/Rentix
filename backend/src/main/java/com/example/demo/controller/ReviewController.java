package com.example.demo.controller;

import com.example.demo.model.Review;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @RequestBody Review review
    ) {
        try {
            return ResponseEntity.ok(reviewService.addReview(productId, SecurityUtils.currentUserId(), review));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviews(productId));
    }

    @GetMapping("/{productId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewStats(productId));
    }
}