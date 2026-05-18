package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationGuard {

    private final UserRepository userRepository;

    public void requireVerified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizator inexistent."));
        requireVerified(user);
    }

    public void requireVerified(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Trebuie să fii autentificat.");
        }
        UserRole role = UserRole.fromString(user.getRole());
        if (role.isAtLeast(UserRole.MODERATOR)) {
            return;
        }
        if (user.isVerified()) {
            return;
        }
        throw new IllegalArgumentException(
                "Verifică-ți identitatea cu buletinul din profil înainte de această acțiune.");
    }
}
