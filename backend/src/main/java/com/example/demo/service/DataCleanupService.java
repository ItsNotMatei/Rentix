package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DataCleanupService {

    private static final Logger log = LoggerFactory.getLogger(DataCleanupService.class);

    private static final Set<String> STAFF_EMAILS = Set.of(
            "moderator@rentix.test",
            "admin@rentix.test",
            "super@rentix.test"
    );

    private final DirectMessageRepository directMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReservationRepository reservationRepository;
    private final OfferRepository offerRepository;
    private final MarketplaceOrderRepository marketplaceOrderRepository;
    private final ModerationReportRepository moderationReportRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final ImagineRepository imagineRepository;
    private final ProductRepository productRepository;
    private final AnuntRepository anuntRepository;
    private final TransactionRepository transactionRepository;
    private final AuthTokenRepository authTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void cleanupAllExceptStaff() {
        log.warn("[Rentix] Curățare date demo — se păstrează doar conturile staff.");

        directMessageRepository.deleteAllInBatch();
        conversationRepository.deleteAllInBatch();
        reviewRepository.deleteAllInBatch();
        favoriteRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        offerRepository.deleteAllInBatch();
        marketplaceOrderRepository.deleteAllInBatch();
        moderationReportRepository.deleteAllInBatch();
        identityVerificationRepository.deleteAllInBatch();
        imagineRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        anuntRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
        authTokenRepository.deleteAllInBatch();

        userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && !STAFF_EMAILS.contains(u.getEmail().toLowerCase()))
                .forEach(u -> {
                    refreshTokenRepository.deleteByUser(u);
                    userRepository.delete(u);
                });

        refreshTokenRepository.deleteAllInBatch();

        log.info("[Rentix] Curățare finalizată. Utilizatori rămași: {}", userRepository.count());
    }
}
