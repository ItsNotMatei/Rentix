package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String comentariu;

    @ManyToOne
    @JoinColumn(name = "anunt_id")
    private Anunt anunt;
}