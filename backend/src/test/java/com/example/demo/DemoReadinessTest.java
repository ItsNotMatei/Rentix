package com.example.demo;

import com.example.demo.model.EscrowStatus;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.AuthTokenRepository;
import com.example.demo.repository.MarketplaceOrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MarketplacePaymentService;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verificări critice pentru prezentarea demo: autentificare staff, 2FA utilizatori,
 * publicare anunțuri cu descrieri lungi, configurare Stripe și finalizare plată.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DemoReadinessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private MarketplaceOrderRepository orderRepository;

    @Autowired
    private MarketplacePaymentService marketplacePaymentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("Autentificare demo")
    class AuthDemo {

        @Test
        @DisplayName("Staff ADMIN primește token direct, fără 2FA")
        void staffAdminBypassesTwoFactor() throws Exception {
            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"admin@rentix.test","password":"Admin12345!"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.user.role").value("ADMIN"))
                    .andExpect(jsonPath("$.requiresTwoFactor").doesNotExist());
        }

        @Test
        @DisplayName("Staff MODERATOR primește token direct, fără 2FA")
        void staffModeratorBypassesTwoFactor() throws Exception {
            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"moderator@rentix.test","password":"Mod12345!"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.user.role").value("MODERATOR"))
                    .andExpect(jsonPath("$.requiresTwoFactor").doesNotExist());
        }

        @Test
        @DisplayName("Staff SUPER_ADMIN primește token direct, fără 2FA")
        void staffSuperAdminBypassesTwoFactor() throws Exception {
            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"super@rentix.test","password":"Super12345!"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.user.role").value("SUPER_ADMIN"))
                    .andExpect(jsonPath("$.requiresTwoFactor").doesNotExist());
        }

        @Test
        @DisplayName("Utilizator normal trece prin 2FA")
        void regularUserRequiresTwoFactor() throws Exception {
            String email = "demo-user-2fa@rentix.test";
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nume":"Demo User","email":"%s","password":"Test1234!"}
                                    """.formatted(email)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"%s","password":"Test1234!"}
                                    """.formatted(email)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.requiresTwoFactor").value(true))
                    .andExpect(jsonPath("$.challengeId").exists())
                    .andExpect(jsonPath("$.accessToken").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Publicare anunțuri")
    class ListingDemo {

        @Test
        @DisplayName("Descriere lungă (>500 caractere) se salvează fără eroare")
        void createProductWithLongDescription() throws Exception {
            var signup = mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nume":"Lister Demo","email":"long-desc@rentix.test","password":"Test1234!"}
                                    """))
                    .andExpect(status().isOk())
                    .andReturn();

            String longDescriere = "A".repeat(1200);
            String productJson = """
                    {
                      "titlu":"Produs demo descriere lungă",
                      "descriere":"%s",
                      "pret":"50",
                      "adresa":"București, Demo Street 1",
                      "tip":"inchiriere",
                      "categorie":"Gadgeturi",
                      "stareProdus":"NOU",
                      "imagineUrl":"https://res.cloudinary.com/demo/image/upload/v1/rentix/demo.jpg"
                    }
                    """.formatted(longDescriere);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson)
                            .cookie(signup.getResponse().getCookies()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.titlu").value("Produs demo descriere lungă"));

            Product saved = productRepository.findAll().stream()
                    .filter(p -> "Produs demo descriere lungă".equals(p.getTitlu()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(1200, saved.getDescriere().length());
        }
    }

    @Nested
    @DisplayName("Plăți Stripe")
    class StripeDemo {

        @Test
        @DisplayName("Endpoint config expune cheia publicabilă")
        void paymentsConfigReturnsPublishableKey() throws Exception {
            mockMvc.perform(get("/api/payments/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.publishableKey").exists());
        }

        @Test
        @DisplayName("checkout.session.completed marchează comanda și produsul")
        void checkoutCompletedMarksOrderAndProductSold() throws Exception {
            User seller = new User();
            seller.setEmail("seller-demo-readiness@rentix.test");
            seller.setNume("Seller Demo");
            seller.setPassword(passwordEncoder.encode("Test1234!"));
            seller.setRole("USER");
            seller = userRepository.save(seller);

            User buyer = new User();
            buyer.setEmail("buyer-demo-readiness@rentix.test");
            buyer.setNume("Buyer Demo");
            buyer.setPassword(passwordEncoder.encode("Test1234!"));
            buyer.setRole("USER");
            buyer.setVerified(true);
            buyer = userRepository.save(buyer);

            Product product = new Product();
            product.setTitlu("Produs plată demo");
            product.setDescriere("Test Stripe checkout");
            product.setPret(150.0);
            product.setAdresa("Cluj-Napoca");
            product.setStatus("AVAILABLE");
            product.setUserId(seller.getId());
            product = productRepository.save(product);

            var order = new com.example.demo.model.MarketplaceOrder();
            order.setListingId(product.getId());
            order.setBuyerId(buyer.getId());
            order.setSellerId(seller.getId());
            order.setAmountCents(15000L);
            order.setEscrowStatus(EscrowStatus.PENDING_PAYMENT);
            order.setStripeCheckoutSessionId("cs_demo_readiness");
            order.setBuyNow(true);
            orderRepository.save(order);

            Session session = mock(Session.class);
            when(session.getId()).thenReturn("cs_demo_readiness");
            when(session.getPaymentIntent()).thenReturn("pi_demo_readiness");

            marketplacePaymentService.onCheckoutCompleted(session);

            var updatedOrder = orderRepository.findByStripeCheckoutSessionId("cs_demo_readiness").orElseThrow();
            assertEquals(EscrowStatus.ESCROW_ACTIVE, updatedOrder.getEscrowStatus());

            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertEquals("SOLD", updatedProduct.getStatus());
        }
    }
}
