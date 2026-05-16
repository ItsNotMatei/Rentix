package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.model.Review;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Review addReview(Long productId, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produs inexistent"));

        // Reparat: Se folosește setAnunt deoarece proprietatea din model se numește anunt
        review.setAnunt(product);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public List<Review> getReviews(Long productId) {
        // Asigură-te că în ReviewRepository ai definită metoda findByAnuntId(Long id)
        // În caz că dă eroare aici, poți schimba în reviewRepository.findByAnunt_Id(productId);
        return reviewRepository.findByAnuntId(productId);
    }
}