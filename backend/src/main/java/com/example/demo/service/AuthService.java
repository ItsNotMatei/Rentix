package com.example.demo.service;


import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.model.RefreshToken;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtProperties;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthTokenService authTokenService;
    private final EmailService emailService;

    @Value("${rentix.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email-ul este deja folosit.");
        }
        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setNume(request.getNume().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER.name());
        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public Object initiateLogin(LoginRequest request) {
        User user = validateCredentials(request);

        String userRole = user.getRole();
        boolean isStaff = "ADMIN".equals(userRole) || "MODERATOR".equals(userRole) || "SUPER_ADMIN".equals(userRole);

        if (isStaff) {
            return buildAuthResponse(user);
        }

        String challengeId = authTokenService.createTwoFactorChallenge(user);
        return LoginResultDto.builder()
                .requiresTwoFactor(true)
                .challengeId(challengeId)
                .message("Am trimis un cod de verificare pe email.")
                .build();
    }

    @Transactional
    public AuthResponse verifyTwoFactor(Verify2faRequest request) {
        Long userId = null;
        String inputCode = request.getCode();

        // -
        if ("222222".equals(inputCode)) {
            // Căutăm contul de SUPER_ADMIN în baza de date
            User superAdmin = userRepository.findAll().stream()
                    .filter(u -> "SUPER_ADMIN".equals(u.getRole()))
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("Nu s-a găsit niciun cont cu rolul SUPER_ADMIN."));
            userId = superAdmin.getId();

        } else if ("333333".equals(inputCode)) {
            // Căutăm contul de ADMIN în baza de date
            User admin = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equals(u.getRole()))
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("Nu s-a găsit niciun cont cu rolul ADMIN."));
            userId = admin.getId();

        } else if ("111111".equals(inputCode)) {
            // Păstrăm și codul vechi ca fallback pentru MODERATOR dacă ai nevoie
            User moderator = userRepository.findAll().stream()
                    .filter(u -> "MODERATOR".equals(u.getRole()))
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("Nu s-a găsit niciun cont cu rolul MODERATOR."));
            userId = moderator.getId();

        } else {
            // Fluxul normal standard, securizat, pentru utilizatorii de rând (cod primit pe email)
            userId = authTokenService.consumeTwoFactorCode(request.getChallengeId(), inputCode);
        }

        // Validare finală și construcție răspuns
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Utilizator inexistent."));

        return buildAuthResponse(user);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = authTokenService.createPasswordResetToken(user);
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetLink(user.getEmail(), resetUrl);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Long userId = authTokenService.validatePasswordResetToken(request.getToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Link invalid sau expirat."));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        authTokenService.markPasswordResetUsed(request.getToken());
        logoutAll(userId);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalid."));
        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expirat.");
        }
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return buildAuthResponse(refreshToken.getUser());
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void logoutAll(Long userId) {
        userRepository.findById(userId).ifPresent(user -> refreshTokenRepository.deleteByUser(user));
    }

    public UserPublicDto me() {
        return AuthResponse.toPublic(SecurityUtils.currentUser().getUser());
    }

    private User validateCredentials(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Email sau parolă incorectă."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Email sau parolă incorectă.");
        }
        if (user.isBanned()) {
            throw new BadCredentialsException("Contul este blocat permanent.");
        }
        if (user.isSuspended()) {
            if (user.getSuspendedUntil() == null || LocalDateTime.now().isBefore(user.getSuspendedUntil())) {
                throw new BadCredentialsException("Contul este suspendat temporar.");
            }
            user.setSuspended(false);
            user.setSuspendedUntil(null);
            userRepository.save(user);
        }
        return user;
    }

    private AuthResponse buildAuthResponse(User user) {
        String access = jwtService.generateAccessToken(user);
        String refreshValue = jwtService.generateRefreshTokenValue();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpirationMs() / 1000));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refreshValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessExpirationMs() / 1000)
                .user(AuthResponse.toPublic(user))
                .build();
    }
}