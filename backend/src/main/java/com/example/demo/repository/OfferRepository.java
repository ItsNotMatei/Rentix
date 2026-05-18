package com.example.demo.repository;

import com.example.demo.model.Offer;
import com.example.demo.model.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
    List<Offer> findByListingIdAndStatus(Long listingId, OfferStatus status);
    Optional<Offer> findByIdAndSellerId(Long id, Long sellerId);
    Optional<Offer> findByIdAndBuyerId(Long id, Long buyerId);
    boolean existsByListingIdAndBuyerIdAndStatusIn(Long listingId, Long buyerId, List<OfferStatus> statuses);

    void deleteByListingId(Long listingId);
}
