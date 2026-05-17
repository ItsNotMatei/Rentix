package com.example.demo.controller;

import com.example.demo.model.IdentityVerification;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.IdentityVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Permite apelul din React
public class IdentityController {

    private final IdentityVerificationService identityService;
    private final UserRepository userRepository; // <-- Injectăm repository-ul de utilizatori

    @PostMapping("/create-session")
    public ResponseEntity<?> createSession() throws Exception {
        Long userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(identityService.createStripeIdentitySession(userId));
    }

    @PostMapping("/webhook-verified")
    public ResponseEntity<?> webhookVerified(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        Long userId = SecurityUtils.currentUserId();

        if (sessionId != null) {
            identityService.handleVerifiedSession(sessionId);
        }


        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVerified(true);
            user.setVerificationStatus("VERIFIED");
            user.setVerifiedAt(LocalDateTime.now());
            user.setVerificationProvider("STRIPE_IDENTITY");

            userRepository.save(user); // Salvează modificările instant în MySQL!
            System.out.println("Sistem: Utilizatorul cu ID " + userId + " a fost verificat automat în baza de date.");
        }

        return ResponseEntity.ok(identityService.getUserPublicAfterVerify(userId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> stripeIdentityWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) throws Exception {
        identityService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        IdentityVerification v = identityService.getStatus(SecurityUtils.currentUserId());
        if (v == null) {
            return ResponseEntity.ok(Map.of("status", "NONE"));
        }
        return ResponseEntity.ok(Map.of(
                "status", v.getStatus().name(),
                "provider", v.getVerificationProvider(),
                "verifiedAt", v.getVerifiedAt()
        ));
    }
}
