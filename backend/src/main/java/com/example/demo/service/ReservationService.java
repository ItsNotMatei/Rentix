package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VerificationGuard verificationGuard;

    @Autowired
    private ConversationService conversationService;

    @Transactional
    public Reservation createReservation(Long anuntId, Long userId, LocalDate startDate, LocalDate endDate) {
        verificationGuard.requireVerified(userId);

        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Perioada selectată nu este validă.");
        }

        Product product = productRepository.findById(anuntId)
                .orElseThrow(() -> new IllegalArgumentException("Anunț inexistent."));

        if (product.getUserId() != null && product.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Nu poți rezerva propriul anunț.");
        }

        boolean hasConflict = reservationRepository.existsConflictingReservation(
                anuntId, startDate, endDate
        );
        if (hasConflict) {
            throw new IllegalArgumentException("Perioada selectată este deja rezervată.");
        }

        Reservation reservation = new Reservation();
        Anunt anunt = new Anunt();
        anunt.setId(anuntId);
        reservation.setAnunt(anunt);

        User user = new User();
        user.setId(userId);
        reservation.setUser(user);

        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        Reservation saved = reservationRepository.save(reservation);
        notifyOwner(product, userId, startDate, endDate);
        return saved;
    }

    private void notifyOwner(Product product, Long renterId, LocalDate start, LocalDate end) {
        if (product.getUserId() == null) return;
        try {
            Conversation c = conversationService.getOrCreate(product.getId(), renterId, product.getUserId());
            String msg = String.format(
                    "📅 Cerere rezervare: %s → %s pentru „%s”. Verifică calendarul.",
                    start, end, product.getTitlu() != null ? product.getTitlu() : "anunț"
            );
            conversationService.sendMessage(c.getId(), renterId, msg);
        } catch (Exception ignored) {
        }
    }

    public List<Reservation> getReservationsForAnunt(Long anuntId) {
        return reservationRepository.findByAnuntId(anuntId);
    }

    public void updateStatuses() {
        List<Reservation> reservations = reservationRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Reservation res : reservations) {
            if (res.getStatus() == ReservationStatus.CONFIRMED
                    && res.getStartDate() != null
                    && res.getEndDate() != null
                    && !today.isBefore(res.getStartDate())
                    && !today.isAfter(res.getEndDate())) {
                res.setStatus(ReservationStatus.ACTIVE);
                reservationRepository.save(res);
            } else if (res.getStatus() == ReservationStatus.ACTIVE
                    && res.getEndDate() != null
                    && today.isAfter(res.getEndDate())) {
                res.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(res);
            }
        }
    }

    public void expireUnpaidReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Reservation res : reservations) {
            if (res.getStatus() == ReservationStatus.PENDING
                    && res.getCreatedAt() != null
                    && today.isAfter(res.getCreatedAt().plusDays(1))) {
                res.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(res);
            }
        }
    }
}
