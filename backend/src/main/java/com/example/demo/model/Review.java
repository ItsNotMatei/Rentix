package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private int rating;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser; // Userul care primește recenzia

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author; // Userul care scrie recenzia
    @ManyToOne
    @JoinColumn(name = "anunt_id") // Numele coloanei în tabelul SQL
    private Anunt anunt;
}