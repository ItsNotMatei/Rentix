package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDto;
import com.example.demo.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ConversationChatController {

    private final ConversationService conversationService;

    @MessageMapping("/conversations/{conversationId}/send")
    @SendTo("/topic/conversations/{conversationId}")
    public ChatMessageDto send(
            @DestinationVariable Long conversationId,
            @Payload Map<String, Object> payload
    ) {
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = payload.get("content").toString();
        return conversationService.sendMessage(conversationId, senderId, content);
    }

    @MessageMapping("/conversations/{conversationId}/typing")
    @SendTo("/topic/conversations/{conversationId}/typing")
    public Map<String, Object> typing(
            @DestinationVariable Long conversationId,
            @Payload Map<String, Object> payload
    ) {
        payload.put("conversationId", conversationId);
        payload.put("type", "TYPING");
        return payload;
    }
}
