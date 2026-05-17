package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthCookieService;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthCookieService authCookieService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.signup(request);
        authCookieService.writeAuthCookies(response, auth);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse auth = authService.login(request);
        authCookieService.writeAuthCookies(response, auth);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshValue = request != null && request.getRefreshToken() != null
                ? request.getRefreshToken()
                : authCookieService.readRefreshToken(httpRequest);
        if (refreshValue == null || refreshValue.isBlank()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Refresh token lipsă.");
        }
        AuthResponse auth = authService.refresh(refreshValue);
        authCookieService.writeAuthCookies(httpResponse, auth);
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refresh = request != null && request.getRefreshToken() != null
                ? request.getRefreshToken()
                : authCookieService.readRefreshToken(httpRequest);
        if (refresh != null) {
            authService.logout(refresh);
        }
        authCookieService.clearAuthCookies(httpResponse);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserPublicDto> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserPublicDto> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Long currentId = SecurityUtils.currentUserId();
        if (!currentId.equals(id) && !SecurityUtils.currentRole().isAtLeast(com.example.demo.model.UserRole.ADMIN)) {
            throw new org.springframework.security.access.AccessDeniedException("Nu poți edita acest profil.");
        }
        return userRepository.findById(id).map(user -> {
            if (userDetails.getNume() != null) user.setNume(userDetails.getNume());
            if (userDetails.getProfilePic() != null) user.setProfilePic(userDetails.getProfilePic());
            if (userDetails.getTelefon() != null) user.setTelefon(userDetails.getTelefon());
            if (userDetails.getAdresa() != null) user.setAdresa(userDetails.getAdresa());
            User saved = userRepository.save(user);
            return ResponseEntity.ok(AuthResponse.toPublic(saved));
        }).orElse(ResponseEntity.notFound().build());
    }
}
