package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seed("moderator@rentix.test", "Moderator Rentix", "Mod12345!", UserRole.MODERATOR);
        seed("admin@rentix.test", "Admin Rentix", "Admin12345!", UserRole.ADMIN);
        seed("super@rentix.test", "Super Admin", "Super12345!", UserRole.SUPER_ADMIN);
    }

    private void seed(String email, String nume, String password, UserRole role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setNume(nume);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role.name());
        user.setVerified(true);
        userRepository.save(user);
        System.out.println("[Rentix] Cont " + role.name() + " creat: " + email + " / " + password);
    }
}
