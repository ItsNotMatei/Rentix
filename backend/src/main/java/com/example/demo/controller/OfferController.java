package com.example.demo.controller;

import com.example.demo.model.Offer;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long listingId = Long.valueOf(body.get("listingId").toString());
        Double amount = Double.valueOf(body.get("amount").toString());
        Long conversationId = body.get("conversationId") != null
                ? Long.valueOf(body.get("conversationId").toString()) : null;
        Offer offer = offerService.createOffer(listingId, SecurityUtils.currentUserId(), amount, conversationId);
        return ResponseEntity.ok(offerService.toDto(offer));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        Offer offer = offerService.acceptOffer(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(offerService.toDto(offer));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        Offer offer = offerService.rejectOffer(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(offerService.toDto(offer));
    }

    @PostMapping("/{id}/counter")
    public ResponseEntity<?> counter(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Double amount = Double.valueOf(body.get("amount").toString());
        Offer offer = offerService.counterOffer(id, SecurityUtils.currentUserId(), amount);
        return ResponseEntity.ok(offerService.toDto(offer));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> byConversation(@PathVariable Long conversationId) {
        return ResponseEntity.ok(
                offerService.getConversationOffers(conversationId).stream().map(offerService::toDto).toList()
        );
    }
}
