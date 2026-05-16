package com.example.demo.repository;

import com.example.demo.model.Reservation; // IMPORTUL CORECT ACUM
import org.springframework.data.jpa.repository.JpaRepository; // IMPORTUL CORECT ACUM
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
    SELECT COUNT(r) > 0
    FROM Reservation r
    WHERE r.anunt.id = :anuntId
    AND r.status IN (
        com.example.demo.model.ReservationStatus.CONFIRMED,
        com.example.demo.model.ReservationStatus.ACTIVE
    )
    AND (
        r.startDate <= :endDate
        AND r.endDate >= :startDate
    )
    """)
    boolean existsConflictingReservation(
            Long anuntId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Reservation> findByAnuntId(Long anuntId);
}