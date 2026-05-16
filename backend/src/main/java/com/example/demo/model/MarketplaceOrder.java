package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_orders", indexes = {
        @Index(name = "idx_order_listing", columnList = "listing_id"),
        @Index(name = "idx_order_buyer", columnList = "buyer_id"),
        @Index(name = "idx_order_escrow", columnList = "escrow_status")
})
@Getter
@Setter
public class MarketplaceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "offer_id")
    private Long offerId;

    @Column(nullable = false)
    private Long amountCents;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_checkout_session_id")
    private String stripeCheckoutSessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", nullable = false)
    private EscrowStatus escrowStatus = EscrowStatus.PENDING_PAYMENT;

    @Column(name = "buy_now", nullable = false)
    private boolean buyNow = false;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;
}
