package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReservationService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {

    private final TransactionRepository transactionRepository;
    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @Value("sk_test_51TTPKdGgzKi2E9mKjCv4A7X6gDdPaGt7Bz7uyfj3glzRRNOZSg1FuqvC8m1lQUwAqWGAfHTaGHoseAgBYzxxwbh100z30a8rU9")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    // =========================================================================
    // 1. FLUXUL TĂU EXISTENT (Plată Rezervare Produs - Rămâne neschimbat)
    // =========================================================================
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

    // =========================================================================
    // 2. FLUXURI NOI: ABONAMENT PRO & MODIFICARE DATE PROFIL
    // =========================================================================

    @PostMapping("/create-subscription-session")
    public ResponseEntity<?> createSubscriptionSession(@RequestBody Map<String, Object> req) {
        try {
            Object userIdObj = req.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "UserId is missing"));
            }
            String userId = userIdObj.toString();

            String priceId = "price_1TX1NTGgzKi2E9mKnlXCaXcL";

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setSuccessUrl("http://localhost:5173/profile?subscription=success&userId=" + userId)
                    .setCancelUrl("http://localhost:5173/profile?subscription=cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(priceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/activate-pro")
    public ResponseEntity<?> activatePro(@RequestParam Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPro(true);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("Utilizatorul nu a fost găsit.");
    }

    @PutMapping("/update-profile/{userId}")
    public ResponseEntity<?> updateProfileData(@PathVariable Long userId, @RequestBody Map<String, String> data) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setNume(data.get("nume"));
            user.setTelefon(data.get("telefon"));
            user.setAdresa(data.get("adresa"));
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("Eroare la salvarea datelor.");
    }

    // =========================================================================
    // 3. FLUX ESCROW / MIDDLE MAN (Auth & Capture Dinamic)
    // =========================================================================

    @PostMapping("/create-escrow-session")
    public ResponseEntity<?> createEscrowSession(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("userId") == null || req.get("productName") == null || req.get("amount") == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Lipsesc parametrii obligatorii (userId, productName, amount)."));
            }

            String userId = req.get("userId").toString();
            String productName = req.get("productName").toString();
            long baseAmount = Long.parseLong(req.get("amount").toString());

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addPaymentMethodType(SessionCreateParams.Mode.PAYMENT.equals(SessionCreateParams.Mode.PAYMENT) ? SessionCreateParams.PaymentMethodType.CARD : null)
                    // --- BLOCARE BANI PE CARD PENTRU INTERMEDIERE ---
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .setCaptureMethod(SessionCreateParams.PaymentIntentData.CaptureMethod.MANUAL)
                                    .build()
                    )
                    .setSuccessUrl("http://localhost:5173/profile?tab=comenzi&payment=authorized")
                    .setCancelUrl("http://localhost:5173/profile?tab=comenzi")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("ron")
                                                    .setUnitAmount(baseAmount * 100) // Transformat în bani/cenți
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Garanție & Chirie: " + productName)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("userId", userId)
                    .build();

            Session session = Session.create(params);

            Map<String, String> response = new HashMap<>();
            response.put("url", session.getUrl());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/capture-payment/{paymentIntentId}")
    public ResponseEntity<?> capturePayment(@PathVariable String paymentIntentId) {
        try {
            // Preluăm intenția blocată anterior pe Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Verificăm dacă plata nu a fost deja încasată
            if ("requires_capture".equals(paymentIntent.getStatus())) {
                PaymentIntent capturedIntent = paymentIntent.capture();

                // Opțional: Aici actualizezi în TransactionRepository statusul tranzacției în "SUCCESSFUL"

                return ResponseEntity.ok(Map.of("status", "success", "message", "Banii au fost eliberați către proprietar!"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Plata nu se află în stadiul de captură manuală. Status actual: " + paymentIntent.getStatus()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}