package com.example.demo.service;

import com.example.demo.dto.ReviewDto;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final MarketplaceOrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDto addReview(Long productId, Long userId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating-ul trebuie să fie între 1 și 5.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produs inexistent."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizator inexistent."));

        if (reviewRepository.existsByUser_IdAndAnunt_Id(userId, productId)) {
            throw new IllegalArgumentException("Ai lăsat deja o recenzie pentru acest anunț.");
        }

        if (!canReviewProduct(userId, productId)) {
            throw new IllegalArgumentException(
                    "Poți lăsa o recenzie doar după o rezervare sau comandă finalizată.");
        }

        Review review = new Review();
        review.setAnunt(product);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");
        review.setCreatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        return toDto(saved, true);
    }

    public boolean canReviewProduct(Long userId, Long productId) {
        if (orderRepository.existsByBuyerIdAndListingIdAndEscrowStatus(
                userId, productId, EscrowStatus.COMPLETED)) {
            return true;
        }
        if (reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(
                userId, productId, ReservationStatus.COMPLETED)) {
            return true;
        }
        if (reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(
                userId, productId, ReservationStatus.ACTIVE)) {
            return true;
        }
        if (reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(
                userId, productId, ReservationStatus.CONFIRMED)) {
            return true;
        }
        LocalDate today = LocalDate.now();
        return reservationRepository.findByAnuntId(productId).stream()
                .anyMatch(r -> r.getUser() != null
                        && userId.equals(r.getUser().getId())
                        && r.getStatus() != ReservationStatus.CANCELLED
                        && r.getEndDate() != null
                        && !r.getEndDate().isAfter(today));
    }

    public List<ReviewDto> getReviewDtos(Long productId) {
        return reviewRepository.findByAnunt_Id(productId).stream()
                .map(r -> toDto(r, hasVerifiedTransaction(r.getUser().getId(), productId)))
                .collect(Collectors.toList());
    }

    private boolean hasVerifiedTransaction(Long userId, Long productId) {
        return orderRepository.existsByBuyerIdAndListingIdAndEscrowStatus(userId, productId, EscrowStatus.COMPLETED)
                || reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(userId, productId, ReservationStatus.COMPLETED)
                || reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(userId, productId, ReservationStatus.ACTIVE)
                || reservationRepository.existsByUser_IdAndAnunt_IdAndStatus(userId, productId, ReservationStatus.CONFIRMED);
    }

    public boolean hasUserReviewed(Long productId, Long userId) {
        return reviewRepository.existsByUser_IdAndAnunt_Id(userId, productId);
    }

    public Map<String, Object> getReviewStats(Long productId) {
        List<Review> reviews = reviewRepository.findByAnunt_Id(productId);
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

    private ReviewDto toDto(Review r, boolean verified) {
        String name = r.getUser() != null && r.getUser().getNume() != null
                ? r.getUser().getNume()
                : "Utilizator";
        return ReviewDto.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userName(name)
                .verifiedPurchase(verified)
                .build();
    }
}
