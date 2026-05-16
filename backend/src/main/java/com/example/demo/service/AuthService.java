package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserPublicDto;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtProperties;
import com.example.demo.security.JwtService;
import com.example.demo.security.RentixUserDetails;
import com.example.demo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
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

    public AuthResponse login(LoginRequest request) {
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
        return buildAuthResponse(user);
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
