package com.example.demo.controller;

import com.example.demo.model.Reservation;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public Reservation createReservation(
            @RequestParam Long anuntId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        Long uid = SecurityUtils.currentUserId();
        reservationService.updateStatuses();
        return reservationService.createReservation(
                anuntId,
                uid,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
    }

    @GetMapping("/availability/{anuntId}")
    public List<String> getUnavailableDates(@PathVariable Long anuntId) {
        var reservations = reservationService.getReservationsForAnunt(anuntId);
        List<String> dates = new ArrayList<>();

        for (var reservation : reservations) {
            LocalDate current = reservation.getStartDate();
            while (!current.isAfter(reservation.getEndDate())) {
                dates.add(current.toString());
                current = current.plusDays(1);
            }
        }
        return dates;
    }

    @PostMapping("/update-statuses")
    public String updateStatuses() {
        reservationService.updateStatuses();
        return "Statuses updated";
    }
}