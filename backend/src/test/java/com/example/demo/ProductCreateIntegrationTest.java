package com.example.demo;

import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductCreateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createProductWhenAuthenticated() throws Exception {
        var signup = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nume":"Demo Lister","email":"lister-demo@rentix.test","password":"Test1234!"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String productJson = """
                {
                  "titlu":"Cameră foto demo",
                  "descriere":"Canon EOS pentru prezentare",
                  "pret":"75",
                  "adresa":"Strada Demo 1, București",
                  "tip":"inchiriere",
                  "categorie":"Gadgeturi",
                  "stareProdus":"PUTIN_FOLOSIT",
                  "imagineUrl":"https://res.cloudinary.com/demo/image/upload/v1/rentix/camera.jpg"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson)
                        .cookie(signup.getResponse().getCookies()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titlu").value("Cameră foto demo"))
                .andExpect(jsonPath("$.categorie").value("Gadgeturi"))
                .andExpect(jsonPath("$.imageUrl").exists());

        assertTrue(productRepository.findAll().stream()
                .anyMatch(p -> "Cameră foto demo".equals(p.getTitlu())));
    }
}
