package com.example.demo.service;

import com.example.demo.config.StripeProperties;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.identity.VerificationSession;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeWebhookHandler {

    private final StripeProperties stripeProperties;
    private final MarketplacePaymentService marketplacePaymentService;
    private final IdentityVerificationService identityVerificationService;
    private final UserRepository userRepository;

    @Value("${rentix.app.production:false}")
    private boolean production;

    public void validateConfiguration() {
        if (production && (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank())) {
            throw new IllegalStateException("STRIPE_WEBHOOK_SECRET obligatoriu în production.");
        }
    }

    public void handle(String payload, String sigHeader) throws Exception {
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            if (production) {
                throw new IllegalStateException("Webhook Stripe neconfigurat.");
            }
            return;
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            throw new IllegalArgumentException("Lipsește headerul Stripe-Signature.");
        }
        Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        String type = event.getType();

        if ("checkout.session.completed".equals(type)) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;
            if ("subscription".equals(session.getMode())) {
                activateProFromSession(session);
            } else {
                marketplacePaymentService.onCheckoutCompleted(session);
            }
            return;
        }

        if ("identity.verification_session.verified".equals(type)) {
            VerificationSession vs = (VerificationSession) event.getDataObjectDeserializer().getObject().orElse(null);
            if (vs != null) {
                identityVerificationService.handleVerifiedSession(vs.getId());
            }
        }
    }

    private void activateProFromSession(Session session) {
        String userIdStr = session.getClientReferenceId();
        if (userIdStr == null && session.getMetadata() != null) {
            userIdStr = session.getMetadata().get("userId");
        }
        if (userIdStr == null) return;
        try {
            Long userId = Long.parseLong(userIdStr);
            userRepository.findById(userId).ifPresent(user -> {
                user.setPro(true);
                userRepository.save(user);
            });
        } catch (NumberFormatException ignored) {
        }
    }
}
