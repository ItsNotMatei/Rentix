package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationReturnTest {

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User owner;
    private User renter;
    private Product product;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        owner = saveUser("owner-return@test.local", "Owner");
        renter = saveUser("renter@test.local", "Renter");

        product = new Product();
        product.setTitlu("Camera");
        product.setPret(100.0);
        product.setUserId(owner.getId());
        product.setStatus("RESERVED");
        product = productRepository.save(product);
    }

    @Test
    void confirmReturn_setsListingAvailable_whenSafeCondition() {
        Reservation r = completedReservation();
        reservationService.confirmReturn(r.getId(), owner.getId(), "Primit în stare perfectă");

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals("AVAILABLE", updated.getStatus());
        assertTrue(reservationRepository.findById(r.getId()).orElseThrow().getReturnConfirmed());
    }

    @Test
    void confirmReturn_keepsReserved_whenDamaged() {
        Reservation r = completedReservation();
        reservationService.confirmReturn(r.getId(), owner.getId(), "Obiect deteriorat la returnare");

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertEquals("RESERVED", updated.getStatus());
    }

    private Reservation completedReservation() {
        Reservation r = new Reservation();
        Anunt anunt = new Anunt();
        anunt.setId(product.getId());
        User ownerRef = new User();
        ownerRef.setId(owner.getId());
        anunt.setUser(ownerRef);
        r.setAnunt(anunt);
        User u = new User();
        u.setId(renter.getId());
        r.setUser(u);
        r.setStartDate(LocalDate.now().minusDays(5));
        r.setEndDate(LocalDate.now().minusDays(1));
        r.setStatus(ReservationStatus.COMPLETED);
        r.setReturnConfirmed(false);
        return reservationRepository.save(r);
    }

    private User saveUser(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("Pass12345!"));
        u.setNume(name);
        u.setRole("USER");
        u.setVerified(true);
        return userRepository.save(u);
    }
}
