package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_reports")
@Getter
@Setter
public class ModerationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "reporter_id")
    private Long reporterId;

    private LocalDateTime createdAt = LocalDateTime.now();
}
