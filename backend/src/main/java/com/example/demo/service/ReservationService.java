package com.example.demo.service;

import com.example.demo.model.Reservation;
import com.example.demo.model.ReservationStatus;
import com.example.demo.model.Anunt; // SCHIMBAT DIN Product ÎN Anunt
import com.example.demo.model.User;
import com.example.demo.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public Reservation createReservation(Long anuntId, Long userId, LocalDate startDate, LocalDate endDate) {
        Reservation reservation = new Reservation();

        // REPARAT: Acum instanțiem Anunt, exact așa cum cere setAnunt()
        Anunt anunt = new Anunt();
        anunt.setId(anuntId);
        reservation.setAnunt(anunt);

        User user = new User();
        user.setId(userId);
        reservation.setUser(user);

        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        boolean hasConflict = reservationRepository.existsConflictingReservation(
                anuntId,
                startDate,
                endDate
        );

        if (hasConflict) {
            throw new RuntimeException("Perioada selectată este deja rezervată");
        }

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsForAnunt(Long anuntId) {
        return reservationRepository.findByAnuntId(anuntId);
    }

    public void updateStatuses() {
        List<Reservation> reservations = reservationRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Reservation res : reservations) {
            if (res.getStatus() == ReservationStatus.CONFIRMED && !today.isBefore(res.getStartDate()) && !today.isAfter(res.getEndDate())) {
                res.setStatus(ReservationStatus.ACTIVE);
                reservationRepository.save(res);
            }
            // Dacă ai starea COMPLETED în enum, lasă așa. Dacă nu, schimbă cu ce ai (ex: FINALIZATA)
            else if (res.getStatus() == ReservationStatus.ACTIVE && today.isAfter(res.getEndDate())) {
                res.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(res);
            }
        }
    }

    public void expireUnpaidReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Reservation res : reservations) {
            // Verifică dacă starea din baza ta de date este PENDING sau alta (ex: IN_ASTEPTARE)
            if (res.getStatus() == ReservationStatus.PENDING && today.isAfter(res.getCreatedAt().plusDays(1))) {

                // REPARAT: Înlocuiește ANULAT cu starea exactă din enum-ul tău ReservationStatus
                // (Poate fi: CANCELLED, ANULAT, REJECTED, etc.)
                res.setStatus(ReservationStatus.CANCELLED);

                reservationRepository.save(res);
            }
        }
    }
}