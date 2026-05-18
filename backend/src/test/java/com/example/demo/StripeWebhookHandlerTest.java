package com.example.demo;

import com.example.demo.service.StripeWebhookHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class StripeWebhookHandlerTest {

    @Autowired
    private StripeWebhookHandler handler;

    @Test
    void validateConfigurationInTestProfile() {
        assertDoesNotThrow(() -> handler.validateConfiguration());
    }

    @Test
    void rejectsWebhookWhenSignatureMissing() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> handler.handle("{}", null)
        );
    }
}
