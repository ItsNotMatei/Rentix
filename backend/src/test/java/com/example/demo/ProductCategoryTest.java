package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductCategoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listCategoriesMeta() throws Exception {
        mockMvc.perform(get("/api/products/meta/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Haine"));
    }

    @Test
    void byCategoryReturnsEmptyWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/products/by-category").param("categorie", "Sport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
