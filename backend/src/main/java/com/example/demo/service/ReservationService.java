package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.AnuntRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final AnuntRepository anuntRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public Reservation createReservation(
            Long anuntId,
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {


        Anunt anunt = anuntRepository.findByIdForUpdate(anuntId)
                .orElseThrow(() -> new RuntimeException("Anunt not found"));

        boolean conflict = reservationRepository.existsConflictingReservation(
                anuntId,
                startDate,
                endDate
        );

        if (conflict) {
            throw new RuntimeException("Anunt already reserved in this period");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reservation reservation = new Reservation();
        reservation.setAnunt(anunt);
        reservation.setUser(user);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setCreatedAt(LocalDate.now());


        reservation.setStatus(ReservationStatus.PENDING);
        anunt.setStatus(AnuntStatus.RESERVED);
        anuntRepository.save(anunt);

        Reservation saved = reservationRepository.save(reservation);

        messagingTemplate.convertAndSend(
                "/topic/availability",
                "NEW_RESERVATION"
        );

        return saved;
    }
    public List<Reservation> getReservationsForAnunt(Long anuntId) {
        return reservationRepository.findByAnuntId(anuntId);
    }

    // -----------------------------
    // UPDATE STATUSES (scheduler)
    // -----------------------------
    @Transactional
    public void updateStatuses() {

        LocalDate today = LocalDate.now();

        List<Reservation> reservations = reservationRepository.findAll();

        for (Reservation r : reservations) {

            // ACTIVE
            if (r.getStatus() == ReservationStatus.CONFIRMED
                    && !today.isBefore(r.getStartDate())
                    && !today.isAfter(r.getEndDate())) {

                r.setStatus(ReservationStatus.ACTIVE);

                Anunt anunt = r.getAnunt();
                anunt.setStatus(AnuntStatus.RENTED);
                anuntRepository.save(anunt);

                messagingTemplate.convertAndSend(
                        "/topic/availability",
                        "RENTED"
                );
            }

            // COMPLETED
            if (r.getStatus() == ReservationStatus.ACTIVE
                    && today.isAfter(r.getEndDate())) {

                r.setStatus(ReservationStatus.COMPLETED);

                Anunt anunt = r.getAnunt();
                anunt.setStatus(AnuntStatus.AVAILABLE);
                anuntRepository.save(anunt);

                messagingTemplate.convertAndSend(
                        "/topic/availability",
                        "AVAILABLE"
                );
            }
        }
    }

    @Transactional
    public void expireUnpaidReservations() {

        List<Reservation> reservations = reservationRepository.findAll();

        for (Reservation r : reservations) {

            if (r.getStatus() == ReservationStatus.PENDING) {

                LocalDate expireAt = r.getCreatedAt().plusDays(1);

                if (LocalDate.now().isAfter(expireAt)) {

                    r.setStatus(ReservationStatus.EXPIRED);

                    Anunt anunt = r.getAnunt();
                    anunt.setStatus(AnuntStatus.AVAILABLE);

                    anuntRepository.save(anunt);

                    messagingTemplate.convertAndSend(
                            "/topic/availability",
                            "EXPIRED"
                    );
                }
            }
        }
    }

    @Transactional
    public void cancelReservation(Long reservationId) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setStatus(ReservationStatus.CANCELLED);

        Anunt anunt = r.getAnunt();
        anunt.setStatus(AnuntStatus.AVAILABLE);

        anuntRepository.save(anunt);

        messagingTemplate.convertAndSend(
                "/topic/availability",
                "CANCELLED"
        );
    }
    public boolean isAvailable(Long anuntId,
                               LocalDate startDate,
                               LocalDate endDate) {

        return !reservationRepository.existsConflictingReservation(
                anuntId,
                startDate,
                endDate
        );
    }
}