package com.example.demo.repository;

import com.example.demo.model.EscrowStatus;
import com.example.demo.model.MarketplaceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarketplaceOrderRepository extends JpaRepository<MarketplaceOrder, Long> {
    Optional<MarketplaceOrder> findByStripeCheckoutSessionId(String sessionId);
    Optional<MarketplaceOrder> findByStripePaymentIntentId(String paymentIntentId);
    List<MarketplaceOrder> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
    boolean existsByListingIdAndEscrowStatusIn(Long listingId, List<EscrowStatus> statuses);

    boolean existsByBuyerIdAndListingIdAndEscrowStatus(Long buyerId, Long listingId, EscrowStatus status);

    List<MarketplaceOrder> findByListingIdAndEscrowStatus(Long listingId, EscrowStatus escrowStatus);

    List<MarketplaceOrder> findByListingIdAndEscrowStatusAndCreatedAtBefore(
            Long listingId, EscrowStatus escrowStatus, LocalDateTime createdAtBefore);
}
