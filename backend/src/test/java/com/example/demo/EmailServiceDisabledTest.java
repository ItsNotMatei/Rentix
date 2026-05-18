package com.example.demo;

import com.example.demo.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "rentix.mail.enabled=false")
class EmailServiceDisabledTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void logsOnlyWhenMailDisabled() {
        emailService.sendTwoFactorCode("demo@rentix.test", "654321");
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}
