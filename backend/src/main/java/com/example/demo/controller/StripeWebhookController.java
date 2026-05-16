package com.example.demo.controller;

import com.example.demo.service.MarketplacePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final MarketplacePaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader
    ) throws Exception {
        paymentService.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok("ok");
    }
}
