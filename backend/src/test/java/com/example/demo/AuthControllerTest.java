package com.example.demo;

import com.example.demo.repository.AuthTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Test
    void signupSetsHttpOnlyCookies() throws Exception {
        String body = """
                {"nume":"Test User","email":"testcookie@rentix.test","password":"Test1234!"}
                """;

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(cookie().exists("rentix_access"))
                .andExpect(cookie().httpOnly("rentix_access", true));
    }

    @Test
    void signinRejectsBadPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"none@rentix.test\",\"password\":\"WrongPass1!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signinRequiresTwoFactorThenVerify() throws Exception {
        String email = "twofa@rentix.test";
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nume\":\"2FA User\",\"email\":\"" + email + "\",\"password\":\"Test1234!\"}"))
                .andExpect(status().isOk());

        var signinResult = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Test1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresTwoFactor").value(true))
                .andExpect(jsonPath("$.challengeId").exists())
                .andReturn();

        String response = signinResult.getResponse().getContentAsString();
        String challengeId = response.replaceAll("(?s).*\"challengeId\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        String code = authTokenRepository.findById(challengeId).orElseThrow().getCode();

        mockMvc.perform(post("/api/auth/verify-2fa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"challengeId\":\"" + challengeId + "\",\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("rentix_access"));
    }
}
