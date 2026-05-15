package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Permite cererile de la React
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/{id}/update-profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody User profileData) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilizatorul nu a fost găsit.");
        }

        User user = userOptional.get();
        user.setNume(profileData.getNume());
        user.setTelefon(profileData.getTelefon());
        user.setAdresa(profileData.getAdresa());

        userRepository.save(user);
        return ResponseEntity.ok(user); // Trimitem utilizatorul actualizat înapoi la React
    }
}