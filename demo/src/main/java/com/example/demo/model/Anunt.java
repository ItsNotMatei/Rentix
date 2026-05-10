package com.example.demo.model;
import org.springframework.data.elasticsearch.annotations.Document;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "anunturi")
@Document(indexName = "anunturi")
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Aici se face legătura: un anunț are UN SINGUR creator

    @OneToMany(mappedBy = "anunt", cascade = CascadeType.ALL)
    private List<ImagineAnunt> imagini;

    @OneToMany(mappedBy = "anunt", cascade = CascadeType.ALL)
    private List<Review> reviews;
    @Enumerated(EnumType.STRING)
    private AnuntStatus status = AnuntStatus.AVAILABLE;

}