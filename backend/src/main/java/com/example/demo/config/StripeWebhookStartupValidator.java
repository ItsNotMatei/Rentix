package com.example.demo.config;

import com.example.demo.service.StripeWebhookHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeWebhookStartupValidator {

    private final StripeProperties stripeProperties;
    private final StripeWebhookHandler webhookHandler;

    @Value("${rentix.app.production:false}")
    private boolean production;

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        webhookHandler.validateConfiguration();
        if (production && (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank())) {
            log.error("[Rentix] PRODUCTION: STRIPE_WEBHOOK_SECRET lipsește — plățile nu se confirmă automat în DB!");
        } else if (!production && stripeProperties.getWebhookSecret().isBlank()) {
            log.warn("[Rentix] Dev: STRIPE_WEBHOOK_SECRET neconfigurat — webhook-urile Stripe sunt ignorate.");
        } else {
            log.info("[Rentix] Stripe webhook configurat.");
        }
    }
}
