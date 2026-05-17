package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "anunturi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double pret;
    private String titlu;
    private String descriere;
    private String adresa;
    private String tip;
    private String status;

    @Column(name = "user_id")
    private Long userId;

    private Double latitude;
    private Double longitude;

}