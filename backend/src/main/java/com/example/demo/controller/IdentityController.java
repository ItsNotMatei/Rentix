package com.example.demo.controller;

import com.example.demo.model.IdentityVerification;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.IdentityVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class IdentityController {

    private final IdentityVerificationService identityService;

    @PostMapping("/create-session")
    public ResponseEntity<?> createSession() throws Exception {
        Long userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(identityService.createStripeIdentitySession(userId));
    }

    @PostMapping("/webhook-verified")
    public ResponseEntity<?> webhookVerified(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        if (sessionId != null) {
            identityService.handleVerifiedSession(sessionId);
        }
        return ResponseEntity.ok(identityService.getUserPublicAfterVerify(SecurityUtils.currentUserId()));
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
