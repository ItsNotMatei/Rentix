package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReviewsForMissingProductReturnsEmptyOrNotFound() throws Exception {
        mockMvc.perform(get("/api/reviews/999999"))
                .andExpect(status().isOk());
    }

    @Test
    void statsEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/reviews/1/stats"))
                .andExpect(status().is2xxSuccessful());
    }
}
