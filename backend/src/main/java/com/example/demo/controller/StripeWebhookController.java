package com.example.demo.controller;

import com.example.demo.service.StripeWebhookHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookHandler webhookHandler;

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) throws Exception {
        webhookHandler.handle(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }
}
