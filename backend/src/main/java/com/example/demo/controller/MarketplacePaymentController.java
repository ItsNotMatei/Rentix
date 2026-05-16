package com.example.demo.controller;

import com.example.demo.model.MarketplaceOrder;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.MarketplacePaymentService;
import com.example.demo.config.StripeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class MarketplacePaymentController {

    private final MarketplacePaymentService paymentService;
    private final StripeProperties stripeProperties;

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("publishableKey", stripeProperties.getPublishableKey());
    }

    @PostMapping("/buy-now/{listingId}")
    public ResponseEntity<?> buyNow(@PathVariable Long listingId) throws Exception {
        return ResponseEntity.ok(paymentService.createBuyNowCheckout(listingId, SecurityUtils.currentUserId()));
    }

    @PostMapping("/offer-checkout/{offerId}")
    public ResponseEntity<?> offerCheckout(@PathVariable Long offerId) throws Exception {
        return ResponseEntity.ok(paymentService.createOfferPaymentCheckout(offerId, SecurityUtils.currentUserId()));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> myOrders() {
        return ResponseEntity.ok(paymentService.myOrders(SecurityUtils.currentUserId()));
    }

    @PostMapping("/orders/{id}/ship")
    public ResponseEntity<?> ship(@PathVariable Long id) {
        MarketplaceOrder order = paymentService.markShipped(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/orders/{id}/confirm-delivery")
    public ResponseEntity<?> confirm(@PathVariable Long id) throws Exception {
        MarketplaceOrder order = paymentService.confirmDelivery(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(order);
    }

    @PostMapping("/orders/{id}/dispute")
    public ResponseEntity<?> dispute(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(paymentService.openDispute(id, SecurityUtils.currentUserId(), body.get("reason")));
    }

    @PostMapping("/orders/{id}/refund")
    public ResponseEntity<?> refund(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(paymentService.refundOrder(id));
    }
}
