package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChatMessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
    private boolean seen;
    private String type = "MESSAGE";
    private String messageType = "TEXT";
    private Long offerId;
    private String imageUrl;
    private java.util.Map<String, Object> offer;
}
