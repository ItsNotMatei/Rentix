package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stripePaymentIntentId;
    private Long amount;
    private Long reservationId;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();
}