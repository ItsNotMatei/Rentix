package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.model.ReservationStatus;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public Review addReview(Long productId, Long userId, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produs inexistent"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizator inexistent"));

        boolean canReview = reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(
                userId, productId, ReservationStatus.COMPLETED
        );
        if (!canReview) {
            throw new RuntimeException("Poți lăsa o recenzie doar după o rezervare finalizată.");
        }

        review.setAnunt(product);
        review.setUser(user);
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public List<Review> getReviews(Long productId) {
        return reviewRepository.findByAnunt_Id(productId);
    }

    public Map<String, Object> getReviewStats(Long productId) {
        List<Review> reviews = getReviews(productId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("reviewCount", reviews.size());
        if (reviews.isEmpty()) {
            stats.put("averageRating", 0.0);
            return stats;
        }
        double avg = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", Math.round(avg * 10.0) / 10.0);
        return stats;
    }
}
