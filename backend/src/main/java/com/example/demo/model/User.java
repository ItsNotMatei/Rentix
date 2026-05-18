package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Column(name = "is_pro")
    private boolean isPro = false;

    @Column(name = "is_verified")
    private boolean isVerified = false;

    @Column(name = "verification_status", length = 20)
    private String verificationStatus = "PENDING";

    @Column(name = "verified_at")
    private java.time.LocalDateTime verifiedAt;

    @Column(name = "verification_provider", length = 50)
    private String verificationProvider;

    private String stripeCustomerId;
    @Column(nullable = false, length = 30)
    private String role = "USER";

    @Column(nullable = false)
    private boolean banned = false;

    @Column(nullable = false)
    private boolean suspended = false;

    private java.time.LocalDateTime suspendedUntil;

    @Column(length = 500)
    private String banReason;

    private String nume;
    private String profilePic;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Anunt> anunturi;
    private String telefon;
    private String adresa;

    /** Sold disponibil în RON (creditează la finalizarea tranzacțiilor). */
    @Column(nullable = false)
    private double balance = 0.0;
}
