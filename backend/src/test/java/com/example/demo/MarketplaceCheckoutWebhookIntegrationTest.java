package com.example.demo;

import com.example.demo.model.EscrowStatus;
import com.example.demo.model.MarketplaceOrder;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.MarketplaceOrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MarketplacePaymentService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MarketplaceCheckoutWebhookIntegrationTest {

    @Autowired
    private MarketplacePaymentService marketplacePaymentService;

    @Autowired
    private MarketplaceOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SESSION_ID = "cs_test_rentix_integration";

    @BeforeEach
    void seedOrder() {
        User seller = new User();
        seller.setEmail("seller-webhook@rentix.test");
        seller.setNume("Seller");
        seller.setPassword(passwordEncoder.encode("Test1234!"));
        seller.setRole("USER");
        seller = userRepository.save(seller);

        User buyer = new User();
        buyer.setEmail("buyer-webhook@rentix.test");
        buyer.setNume("Buyer");
        buyer.setPassword(passwordEncoder.encode("Test1234!"));
        buyer.setRole("USER");
        buyer.setVerified(true);
        buyer = userRepository.save(buyer);

        Product product = new Product();
        product.setTitlu("Produs webhook test");
        product.setDescriere("Test integrare");
        product.setPret(99.0);
        product.setAdresa("București");
        product.setStatus("AVAILABLE");
        product.setUserId(seller.getId());
        product = productRepository.save(product);

        MarketplaceOrder order = new MarketplaceOrder();
        order.setListingId(product.getId());
        order.setBuyerId(buyer.getId());
        order.setSellerId(seller.getId());
        order.setAmountCents(9900L);
        order.setEscrowStatus(EscrowStatus.PENDING_PAYMENT);
        order.setStripeCheckoutSessionId(SESSION_ID);
        order.setBuyNow(true);
        orderRepository.save(order);
    }

    @Test
    void checkoutSessionCompletedUpdatesEscrowAndMarksProductSold() throws Exception {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn(SESSION_ID);
        when(session.getPaymentIntent()).thenReturn("pi_test_rentix_integration");

        marketplacePaymentService.onCheckoutCompleted(session);

        MarketplaceOrder order = orderRepository.findByStripeCheckoutSessionId(SESSION_ID).orElseThrow();
        assertEquals(EscrowStatus.ESCROW_ACTIVE, order.getEscrowStatus());
        assertEquals("pi_test_rentix_integration", order.getStripePaymentIntentId());

        Product product = productRepository.findById(order.getListingId()).orElseThrow();
        assertEquals("SOLD", product.getStatus());
    }
}
