package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "imagine_anunt")
@Data
public class ImagineAnunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anunt_id")
    private Long anuntId;

    private String url;
}