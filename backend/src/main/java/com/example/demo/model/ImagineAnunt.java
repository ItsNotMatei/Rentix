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

    private String url;
    @ManyToOne
    @JoinColumn(name = "anunt_id", nullable = false)
    private Anunt anunt;
}