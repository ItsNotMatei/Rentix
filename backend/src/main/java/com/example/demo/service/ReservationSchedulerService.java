package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationSchedulerService {

    private final ReservationService reservationService;

    @Scheduled(fixedRate = 3600000)
    public void runStatusUpdate() {

        reservationService.updateStatuses();

        System.out.println("Reservation statuses updated automatically");
    }
    @Scheduled(fixedRate = 3600000)
    public void expireReservations() {

        reservationService.expireUnpaidReservations();

        System.out.println("Expired unpaid reservations checked");
    }
}
