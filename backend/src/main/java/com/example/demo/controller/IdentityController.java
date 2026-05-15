package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.identity.VerificationSession;
import com.stripe.param.identity.VerificationSessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/identity")
@CrossOrigin(origins = "http://localhost:5173")
public class IdentityController {

    @Autowired
    private UserRepository userRepository;

    public IdentityController() {
        Stripe.apiKey = "sk_test_51TTPKdGgzKi2E9mKjCv4A7X6gDdPaGt7Bz7uyfj3glzRRNOZSg1FuqvC8m1lQUwAqWGAfHTaGHoseAgBYzxxwbh100z30a8rU9";
    }
    @PostMapping("/create-session/{id}")
    public ResponseEntity<?> createSession(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            try {
                // Configurare universală prin Mape compatibilă cu orice versiune de Stripe-Java
                Map<String, Object> params = new HashMap<>();
                params.put("type", "document");

                Map<String, Object> documentOptions = new HashMap<>();
                documentOptions.put("require_matching_selfie", true);

                Map<String, Object> options = new HashMap<>();
                options.put("document", documentOptions);
                params.put("options", options);

                // Creare sesiune în serverele Stripe
                VerificationSession session = VerificationSession.create(params);

                // Returnăm URL-ul generat de Stripe către React
                Map<String, String> responseData = new HashMap<>();
                responseData.put("url", session.getUrl());

                return ResponseEntity.ok(responseData);

            } catch (Exception e) {
                System.out.println("Eroare gravă Stripe: " + e.getMessage());
                return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/verify-success/{id}")
    public ResponseEntity<?> verifySuccess(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setVerified(true); // Schimbă is_verified în 1
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }
}