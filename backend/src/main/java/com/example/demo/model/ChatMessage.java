package com.example.demo.model;

import lombok.Data;

@Data
public class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
}
