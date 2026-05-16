package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "identity_verifications")
@Getter
@Setter
public class IdentityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "verification_provider")
    private String verificationProvider = "stripe_identity";

    @Column(name = "session_id")
    private String sessionId;

    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
