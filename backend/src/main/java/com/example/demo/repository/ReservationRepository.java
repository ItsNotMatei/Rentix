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
    AND (
        r.status IN (
            com.example.demo.model.ReservationStatus.CONFIRMED,
            com.example.demo.model.ReservationStatus.ACTIVE
        )
        OR (
            r.status = com.example.demo.model.ReservationStatus.COMPLETED
            AND (r.returnConfirmed = false OR r.returnConfirmed IS NULL)
        )
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

    boolean existsByUser_IdAndAnunt_IdAndStatus(
            Long userId,
            Long anuntId,
            com.example.demo.model.ReservationStatus status
    );

    void deleteByAnunt_Id(Long anuntId);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.anunt a
        JOIN FETCH r.user u
        WHERE a.user.id = :ownerId
        AND r.status = com.example.demo.model.ReservationStatus.COMPLETED
        AND (r.returnConfirmed = false OR r.returnConfirmed IS NULL)
        ORDER BY r.endDate DESC
        """)
    List<Reservation> findPendingReturnsByOwnerId(Long ownerId);
}