package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.service.ReservationService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {

    private final TransactionRepository transactionRepository;
    private final ReservationService reservationService;

    @Value("${stripe.api.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/create-intent")
    public PaymentResponse createIntent(
            @RequestBody PaymentRequest req
    ) throws Exception {

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(req.amount())
                        .setCurrency("ron")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams
                                        .AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Transaction tx = new Transaction();

        tx.setStripePaymentIntentId(intent.getId());
        tx.setAmount(req.amount());
        tx.setReservationId(req.reservationId());
        tx.setStatus("PENDING");

        transactionRepository.save(tx);

        return new PaymentResponse(intent.getClientSecret());
    }

    @PostMapping("/payment-success")
    public String paymentSuccess(

            @RequestParam Long anuntId,
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate

    ) {

        reservationService.createReservation(
                anuntId,
                userId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );

        return "Payment successful and reservation created";
    }
}