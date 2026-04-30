package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "anunturi")
@Data
public class Anunt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titlu;
    private String descriere;
    private String tip; // "inchiriaza" sau "vinde"
    private Double pret;
    private String adresa;
    private String imagineUrl; // Reținem doar calea către fișier (ex: "uploads/casa1.jpg")

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Aici se face legătura: un anunț are UN SINGUR creator
}