package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.AuthService;
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

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
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
