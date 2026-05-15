package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        System.out.println("Cerere signup pentru: " + user.getEmail());
        // Verificăm dacă user-ul există deja (opțional, dar recomandat)
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Eroare: Email-ul este deja folosit!";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Cont creat cu succes!";
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody User loginDetails) {
        System.out.println("Cerere login pentru email: " + loginDetails.getEmail());

        return userRepository.findByEmail(loginDetails.getEmail())
                .map(user -> {
                    // Verificăm dacă parola din DB se potrivește cu cea primită
                    if (passwordEncoder.matches(loginDetails.getPassword(), user.getPassword())) {
                        // Returnăm tot obiectul user (care conține și câmpul 'nume')
                        return ResponseEntity.ok(user);
                    } else {
                        return ResponseEntity.status(401).body("Parolă incorectă!");
                    }
                })
                .orElse(ResponseEntity.status(404).body("Utilizator negăsit!"));
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setNume(userDetails.getNume());
            if(userDetails.getProfilePic() != null) user.setProfilePic(userDetails.getProfilePic());
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

}