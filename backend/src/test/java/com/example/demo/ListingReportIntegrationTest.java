package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ListingReportRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ListingReportIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ListingReportRepository listingReportRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private User reporter;
    private Product listing;

    @BeforeEach
    void setUp() {
        listingReportRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        User owner = new User();
        owner.setEmail("owner-report@test.local");
        owner.setPassword(passwordEncoder.encode("Pass12345!"));
        owner.setNume("Owner");
        owner.setRole("USER");
        owner.setVerified(true);
        owner = userRepository.save(owner);

        reporter = new User();
        reporter.setEmail("reporter@test.local");
        reporter.setPassword(passwordEncoder.encode("Pass12345!"));
        reporter.setNume("Reporter");
        reporter.setRole("USER");
        reporter.setVerified(true);
        reporter = userRepository.save(reporter);

        listing = new Product();
        listing.setTitlu("Test listing");
        listing.setPret(50.0);
        listing.setUserId(owner.getId());
        listing.setStatus("AVAILABLE");
        listing = productRepository.save(listing);
    }

    @Test
    void submitReport_requiresAuth() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anuntId\":" + listing.getId() + ",\"reason\":\"Continut inselator\"}"))
                .andExpect(status().isForbidden());
    }
}
