package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private boolean verifiedPurchase;
}
