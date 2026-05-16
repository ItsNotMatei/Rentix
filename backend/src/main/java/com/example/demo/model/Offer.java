package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "offers", indexes = {
        @Index(name = "idx_offer_listing", columnList = "listing_id"),
        @Index(name = "idx_offer_buyer", columnList = "buyer_id"),
        @Index(name = "idx_offer_status", columnList = "status")
})
@Getter
@Setter
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private OfferStatus status = OfferStatus.PENDING;

    @Column(name = "parent_offer_id")
    private Long parentOfferId;

    @Column(name = "marketplace_order_id")
    private Long marketplaceOrderId;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(2);
}
