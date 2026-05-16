package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.model.Conversation;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public List<Map<String, Object>> list() {
        return conversationService.listForUser(SecurityUtils.currentUserId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", conversationService.unreadTotal(SecurityUtils.currentUserId()));
    }

    @PostMapping
    public Conversation create(@RequestBody Map<String, Long> body) {
        return conversationService.getOrCreate(
                body.get("listingId"),
                SecurityUtils.currentUserId(),
                body.get("otherUserId")
        );
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageDto> messages(@PathVariable Long id) {
        return conversationService.getMessages(id);
    }

    @PostMapping("/{id}/messages")
    public ChatMessageDto send(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        if (body.containsKey("imageUrl")) {
            return conversationService.sendImageMessage(id, SecurityUtils.currentUserId(), body.get("imageUrl").toString());
        }
        String content = body.get("content").toString();
        return conversationService.sendMessage(id, SecurityUtils.currentUserId(), content);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        conversationService.markRead(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok().build();
    }
}
