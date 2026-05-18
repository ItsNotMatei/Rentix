package com.example.demo.service;

import com.example.demo.model.IdentityVerification;
import com.example.demo.model.User;
import com.example.demo.model.VerificationStatus;
import com.example.demo.repository.IdentityVerificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.StripeProperties;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.identity.VerificationSession;
import com.stripe.net.Webhook;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IdentityVerificationService {

    private final IdentityVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final StripeProperties stripeProperties;

    @Value("${rentix.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @PostConstruct
    public void init() {
        if (stripeProperties.getSecretKey() != null && !stripeProperties.getSecretKey().isBlank()) {
            Stripe.apiKey = stripeProperties.getSecretKey();
        }
    }

    @Transactional
    public Map<String, String> createStripeIdentitySession(Long userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Map<String, Object> params = new HashMap<>();
        params.put("type", "document");
        Map<String, Object> documentOptions = new HashMap<>();
        documentOptions.put("require_matching_selfie", true);
        Map<String, Object> options = new HashMap<>();
        options.put("document", documentOptions);
        params.put("options", options);
        params.put("metadata", Map.of("userId", userId.toString()));
        params.put("return_url", frontendUrl + "/profile?tab=cont&verification=success");

        VerificationSession session = VerificationSession.create(params);

        IdentityVerification record = new IdentityVerification();
        record.setUserId(userId);
        record.setSessionId(session.getId());
        record.setStatus(VerificationStatus.PENDING);
        record.setVerificationProvider("stripe_identity");
        verificationRepository.save(record);

        user.setVerificationStatus(VerificationStatus.PENDING.name());
        userRepository.save(user);

        return Map.of("url", session.getUrl(), "sessionId", session.getId());
    }

    @Transactional
    public void handleVerifiedSession(String sessionId) {
        verificationRepository.findBySessionId(sessionId).ifPresent(record -> {
            record.setStatus(VerificationStatus.VERIFIED);
            record.setVerifiedAt(LocalDateTime.now());
            verificationRepository.save(record);

            userRepository.findById(record.getUserId()).ifPresent(user -> {
                user.setVerified(true);
                user.setVerificationStatus(VerificationStatus.VERIFIED.name());
                user.setVerifiedAt(LocalDateTime.now());
                user.setVerificationProvider("stripe_identity");
                userRepository.save(user);
            });
        });
    }

    public IdentityVerification getStatus(Long userId) {
        return verificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
    }

    public void handleStripeWebhook(String payload, String sigHeader) throws Exception {
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            return;
        }
        Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        if ("identity.verification_session.verified".equals(event.getType())) {
            VerificationSession vs = (VerificationSession) event.getDataObjectDeserializer().getObject().orElse(null);
            if (vs != null) {
                handleVerifiedSession(vs.getId());
            }
        }
    }

    public User getUserPublicAfterVerify(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }
}
