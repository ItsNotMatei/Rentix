package com.example.demo.service;

import com.example.demo.model.Anunt;
import com.example.demo.model.Review;
import com.example.demo.repository.AnuntRepository;
import com.example.demo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final AnuntRepository anuntRepo;

    public ReviewService(ReviewRepository reviewRepo, AnuntRepository anuntRepo) {
        this.reviewRepo = reviewRepo;
        this.anuntRepo = anuntRepo;
    }

    public Review addReview(Long anuntId, Review review) {
        Anunt anunt = anuntRepo.findById(anuntId).orElseThrow();
        review.setAnunt(anunt);
        return reviewRepo.save(review);
    }
}