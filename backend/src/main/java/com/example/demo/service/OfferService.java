package com.example.demo.service;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.*;
import com.example.demo.repository.OfferRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final ProductRepository productRepository;
    private final ConversationService conversationService;
    private final VerificationGuard verificationGuard;

    @Transactional
    public Offer createOffer(Long listingId, Long buyerId, Double amount, Long conversationId) {
        verificationGuard.requireVerified(buyerId);
        Product product = productRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Anunț inexistent."));
        if (!"AVAILABLE".equalsIgnoreCase(product.getStatus()) && !"available".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalArgumentException("Produsul nu mai este disponibil pentru oferte.");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new IllegalArgumentException("Nu poți face ofertă la propriul anunț.");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Suma ofertei trebuie să fie pozitivă.");
        }

        List<OfferStatus> active = List.of(OfferStatus.PENDING, OfferStatus.ACCEPTED);
        if (offerRepository.existsByListingIdAndBuyerIdAndStatusIn(listingId, buyerId, active)) {
            throw new IllegalArgumentException("Ai deja o ofertă activă pentru acest produs.");
        }

        if (conversationId == null) {
            Conversation conversation = conversationService.getOrCreate(listingId, buyerId, product.getUserId());
            conversationId = conversation.getId();
        }

        Offer offer = new Offer();
        offer.setListingId(listingId);
        offer.setBuyerId(buyerId);
        offer.setSellerId(product.getUserId());
        offer.setConversationId(conversationId);
        offer.setAmount(amount);
        offer.setStatus(OfferStatus.PENDING);
        Offer saved = offerRepository.save(offer);

        conversationService.sendOfferMessage(conversationId, buyerId, saved);
        return saved;
    }

    @Transactional
    public Offer acceptOffer(Long offerId, Long sellerId) {
        Offer offer = offerRepository.findByIdAndSellerId(offerId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Ofertă inexistentă."));
        if (offer.getStatus() != OfferStatus.PENDING && offer.getStatus() != OfferStatus.COUNTERED) {
            throw new IllegalArgumentException("Oferta nu poate fi acceptată.");
        }
        offer.setStatus(OfferStatus.ACCEPTED);
        offerRepository.save(offer);

        Product product = productRepository.findById(offer.getListingId()).orElseThrow();
        product.setStatus("RESERVED");
        productRepository.save(product);

        conversationService.sendSystemMessage(offer.getConversationId(),
                "Oferta de " + offer.getAmount() + " RON a fost acceptată. Cumpărătorul poate plăti acum.");
        return offer;
    }

    @Transactional
    public Offer rejectOffer(Long offerId, Long sellerId) {
        Offer offer = offerRepository.findByIdAndSellerId(offerId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Ofertă inexistentă."));
        offer.setStatus(OfferStatus.REJECTED);
        offerRepository.save(offer);
        conversationService.sendSystemMessage(offer.getConversationId(), "Oferta a fost refuzată.");
        return offer;
    }

    @Transactional
    public Offer counterOffer(Long offerId, Long sellerId, Double newAmount) {
        Offer parent = offerRepository.findByIdAndSellerId(offerId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Ofertă inexistentă."));
        parent.setStatus(OfferStatus.COUNTERED);
        offerRepository.save(parent);

        Offer counter = new Offer();
        counter.setListingId(parent.getListingId());
        counter.setBuyerId(parent.getBuyerId());
        counter.setSellerId(parent.getSellerId());
        counter.setConversationId(parent.getConversationId());
        counter.setAmount(newAmount);
        counter.setParentOfferId(parent.getId());
        counter.setStatus(OfferStatus.PENDING);
        Offer saved = offerRepository.save(counter);
        conversationService.sendOfferMessage(parent.getConversationId(), sellerId, saved);
        return saved;
    }

    public Map<String, Object> toDto(Offer offer) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", offer.getId());
        dto.put("listingId", offer.getListingId());
        dto.put("buyerId", offer.getBuyerId());
        dto.put("sellerId", offer.getSellerId());
        dto.put("conversationId", offer.getConversationId());
        dto.put("amount", offer.getAmount());
        dto.put("status", offer.getStatus().name());
        dto.put("parentOfferId", offer.getParentOfferId());
        dto.put("marketplaceOrderId", offer.getMarketplaceOrderId());
        dto.put("createdAt", offer.getCreatedAt());
        dto.put("expiresAt", offer.getExpiresAt());
        return dto;
    }

    public List<Offer> getConversationOffers(Long conversationId) {
        return offerRepository.findByConversationIdOrderByCreatedAtDesc(conversationId);
    }
}
