package com.example.demo.service;

import com.example.demo.model.AuthToken;
import com.example.demo.model.AuthTokenType;
import com.example.demo.model.User;
import com.example.demo.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthTokenRepository authTokenRepository;
    private final EmailService emailService;

    @Transactional
    public String createTwoFactorChallenge(User user) {
        authTokenRepository.deleteByUserIdAndType(user.getId(), AuthTokenType.TWO_FACTOR);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        AuthToken token = new AuthToken();
        token.setId(UUID.randomUUID().toString());
        token.setUserId(user.getId());
        token.setType(AuthTokenType.TWO_FACTOR);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        authTokenRepository.save(token);

        emailService.sendTwoFactorCode(user.getEmail(), code);
        return token.getId();
    }

    @Transactional
    public Long consumeTwoFactorCode(String challengeId, String code) {
        AuthToken token = authTokenRepository
                .findByIdAndTypeAndUsedFalseAndExpiresAtAfter(challengeId, AuthTokenType.TWO_FACTOR, LocalDateTime.now())
                .orElseThrow(() -> new BadCredentialsException("Cod invalid sau expirat."));

        if (token.getCode() == null || !token.getCode().equals(code.trim())) {
            throw new BadCredentialsException("Cod invalid sau expirat.");
        }
        token.setUsed(true);
        authTokenRepository.save(token);
        return token.getUserId();
    }

    @Transactional
    public String createPasswordResetToken(User user) {
        authTokenRepository.deleteByUserIdAndType(user.getId(), AuthTokenType.PASSWORD_RESET);

        AuthToken token = new AuthToken();
        token.setId(UUID.randomUUID().toString());
        token.setUserId(user.getId());
        token.setType(AuthTokenType.PASSWORD_RESET);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        authTokenRepository.save(token);
        return token.getId();
    }

    public Long validatePasswordResetToken(String tokenId) {
        AuthToken token = authTokenRepository
                .findByIdAndTypeAndUsedFalseAndExpiresAtAfter(tokenId, AuthTokenType.PASSWORD_RESET, LocalDateTime.now())
                .orElseThrow(() -> new BadCredentialsException("Link invalid sau expirat."));
        return token.getUserId();
    }

    @Transactional
    public void markPasswordResetUsed(String tokenId) {
        authTokenRepository.findById(tokenId).ifPresent(t -> {
            t.setUsed(true);
            authTokenRepository.save(t);
        });
    }
}
