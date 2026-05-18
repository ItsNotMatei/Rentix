package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DataCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataCleanupService dataCleanupService;

    @Value("${rentix.data.cleanup-on-start:false}")
    private boolean cleanupOnStart;

    @Override
    public void run(String... args) {
        if (cleanupOnStart) {
            dataCleanupService.cleanupAllExceptStaff();
        }
        seed("moderator@rentix.test", "Moderator Rentix", "Mod12345!", UserRole.MODERATOR);
        seed("admin@rentix.test", "Admin Rentix", "Admin12345!", UserRole.ADMIN);
        seed("super@rentix.test", "Super Admin", "Super12345!", UserRole.SUPER_ADMIN);
    }

    private void seed(String email, String nume, String password, UserRole role) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setEmail(email);
        user.setNume(nume);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setRole(role.name());
        user.setVerified(true);
        user.setPro(true);
        user.setVerificationStatus("VERIFIED");
        userRepository.save(user);
        System.out.println("[Rentix] Cont staff " + role.name() + ": " + email);
    }
}
