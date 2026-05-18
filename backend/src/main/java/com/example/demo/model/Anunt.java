package com.example.demo.model;
import com.example.demo.model.Review;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "anunturi")
@Data
public class Anunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titlu;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String descriere;
    private String tip;
    private Double pret;
    private String adresa;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "anunt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagineAnunt> imagini;

    @OneToMany(mappedBy = "anunt", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Review> reviews;
    @Enumerated(EnumType.STRING)
    private AnuntStatus status = AnuntStatus.AVAILABLE;

}