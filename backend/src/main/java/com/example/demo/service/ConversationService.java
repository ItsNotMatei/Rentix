package com.example.demo.service;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.*;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.DirectMessageRepository;
import com.example.demo.repository.OfferRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OfferRepository offerRepository;

    public Conversation getOrCreate(Long listingId, Long currentUserId, Long otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("Nu poți deschide un chat cu tine însuți.");
        }
        return conversationRepository.findByListingAndParticipants(listingId, currentUserId, otherUserId)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setListingId(listingId);
                    c.setParticipantOneId(Math.min(currentUserId, otherUserId));
                    c.setParticipantTwoId(Math.max(currentUserId, otherUserId));
                    c.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(c);
                });
    }

    public List<Map<String, Object>> listForUser(Long userId) {
        List<Conversation> conversations = conversationRepository.findByParticipant(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Conversation c : conversations) {
            Long otherId = c.getParticipantOneId().equals(userId) ? c.getParticipantTwoId() : c.getParticipantOneId();
            User other = userRepository.findById(otherId).orElse(null);
            Product listing = c.getListingId() != null ? productRepository.findById(c.getListingId()).orElse(null) : null;

            List<DirectMessage> msgs = messageRepository.findByConversation_IdOrderByCreatedAtAsc(c.getId());
            DirectMessage last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);

            Map<String, Object> row = new HashMap<>();
            row.put("id", c.getId());
            row.put("listingId", c.getListingId());
            row.put("listingTitle", listing != null ? listing.getTitlu() : "Anunț");
            row.put("otherUserId", otherId);
            row.put("otherUserName", other != null && other.getNume() != null ? other.getNume() : "Utilizator");
            row.put("lastMessage", last != null ? last.getContent() : "");
            row.put("lastMessageAt", last != null ? last.getCreatedAt() : c.getUpdatedAt());
            row.put("unreadCount", messageRepository.countByConversation_IdAndSeenFalseAndSenderIdNot(c.getId(), userId));
            result.add(row);
        }
        return result;
    }

    public List<ChatMessageDto> getMessages(Long conversationId) {
        return messageRepository.findByConversation_IdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ChatMessageDto sendMessage(Long conversationId, Long senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversație inexistentă"));

        DirectMessage message = new DirectMessage();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setContent(content.trim());
        message.setMessageType(MessageType.TEXT);
        message.setCreatedAt(LocalDateTime.now());
        message.setSeen(false);

        DirectMessage saved = messageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return toDto(saved);
    }

    @Transactional
    public void markRead(Long conversationId, Long userId) {
        messageRepository.markAsSeen(conversationId, userId);
    }

    public long unreadTotal(Long userId) {
        return conversationRepository.findByParticipant(userId).stream()
                .mapToLong(c -> messageRepository.countByConversation_IdAndSeenFalseAndSenderIdNot(c.getId(), userId))
                .sum();
    }

    @Transactional
    public ChatMessageDto sendOfferMessage(Long conversationId, Long senderId, Offer offer) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        DirectMessage message = new DirectMessage();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setMessageType(MessageType.OFFER);
        message.setOfferId(offer.getId());
        message.setContent("Ofertă: " + offer.getAmount() + " RON");
        message.setSeen(false);
        DirectMessage saved = messageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return toDto(saved);
    }

    @Transactional
    public ChatMessageDto sendSystemMessage(Long conversationId, String text) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        DirectMessage message = new DirectMessage();
        message.setConversation(conversation);
        message.setSenderId(0L);
        message.setMessageType(MessageType.SYSTEM);
        message.setContent(text);
        message.setSeen(false);
        DirectMessage saved = messageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return toDto(saved);
    }

    @Transactional
    public ChatMessageDto sendImageMessage(Long conversationId, Long senderId, String imageUrl) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        DirectMessage message = new DirectMessage();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setMessageType(MessageType.IMAGE);
        message.setImageUrl(imageUrl);
        message.setContent("Imagine");
        message.setSeen(false);
        DirectMessage saved = messageRepository.save(message);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return toDto(saved);
    }

    private ChatMessageDto toDto(DirectMessage m) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(m.getId());
        dto.setConversationId(m.getConversation().getId());
        dto.setSenderId(m.getSenderId());
        dto.setContent(m.getContent());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setSeen(m.isSeen());
        dto.setMessageType(m.getMessageType() != null ? m.getMessageType().name() : "TEXT");
        dto.setOfferId(m.getOfferId());
        dto.setImageUrl(m.getImageUrl());
        if (m.getOfferId() != null) {
            offerRepository.findById(m.getOfferId()).ifPresent(offer -> {
                Map<String, Object> offerMap = new HashMap<>();
                offerMap.put("id", offer.getId());
                offerMap.put("amount", offer.getAmount());
                offerMap.put("status", offer.getStatus().name());
                offerMap.put("buyerId", offer.getBuyerId());
                offerMap.put("sellerId", offer.getSellerId());
                dto.setOffer(offerMap);
            });
        }
        return dto;
    }
}
