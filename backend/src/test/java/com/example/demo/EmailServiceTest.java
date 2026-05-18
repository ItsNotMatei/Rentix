package com.example.demo;

import com.example.demo.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = "rentix.mail.enabled=true")
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void sendsTwoFactorEmailWhenMailEnabled() {
        emailService.sendTwoFactorCode("demo@rentix.test", "123456");
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendsPasswordResetEmailWhenMailEnabled() {
        emailService.sendPasswordResetLink("demo@rentix.test", "http://localhost:5173/reset-password?token=abc");
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }
}
