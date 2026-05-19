package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private LocalDate createdAt = LocalDate.now();
    @Setter
    @ManyToOne
    private Anunt anunt;

    @Setter
    @ManyToOne
    private User user;

    @Setter
    private LocalDate startDate;

    @Setter
    private LocalDate endDate;

    @Setter
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    /** Proprietarul confirmă returnarea obiectului după încheierea perioadei. */
    @Setter
    private Boolean returnConfirmed = false;

    @Setter
    @Column(length = 500)
    private String returnCondition;

    @Setter
    private java.time.LocalDateTime returnConfirmedAt;

    public Reservation() {
    }

}