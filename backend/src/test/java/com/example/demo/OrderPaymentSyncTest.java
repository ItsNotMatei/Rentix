package com.example.demo;

import com.example.demo.model.EscrowStatus;
import com.example.demo.model.MarketplaceOrder;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.MarketplaceOrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MarketplacePaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderPaymentSyncTest {

    @Autowired MarketplacePaymentService paymentService;
    @Autowired MarketplaceOrderRepository orderRepository;
    @Autowired ProductRepository productRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void syncOrder_returnsUnchangedWhenNoStripeSession() throws Exception {
        User buyer = saveUser("buyer-sync@test.local");
        User seller = saveUser("seller-sync@test.local");
        Product p = new Product();
        p.setTitlu("Item");
        p.setPret(10.0);
        p.setUserId(seller.getId());
        p.setStatus("AVAILABLE");
        p = productRepository.save(p);

        MarketplaceOrder order = new MarketplaceOrder();
        order.setListingId(p.getId());
        order.setBuyerId(buyer.getId());
        order.setSellerId(seller.getId());
        order.setAmountCents(1000L);
        order.setEscrowStatus(EscrowStatus.PENDING_PAYMENT);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        MarketplaceOrder synced = paymentService.syncOrderFromStripe(order.getId(), buyer.getId());
        assertEquals(EscrowStatus.PENDING_PAYMENT, synced.getEscrowStatus());
    }

    private User saveUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("Test1234!"));
        u.setNume("Test");
        u.setRole("USER");
        u.setVerified(true);
        return userRepository.save(u);
    }
}
