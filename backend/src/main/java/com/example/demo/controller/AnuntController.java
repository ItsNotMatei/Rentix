package com.example.demo.controller;

import com.example.demo.model.Anunt;
import com.example.demo.model.User;
import com.example.demo.repository.AnuntRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/anunturi")
@CrossOrigin("*") // Asigură CORS global dacă ai nevoie
public class AnuntController {

    @Autowired
    private AnuntRepository anuntRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Anunt> getAll() {
        return anuntRepository.findAll();
    }

    @PostMapping("/adauga")
    public Anunt create(@RequestBody Anunt anunt, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        anunt.setUser(user);
        return anuntRepository.save(anunt);
    }

    // --- ENDPOINT NOU PENTRU SEARCH BAR ---
    @GetMapping("/search")
    public ResponseEntity<List<Anunt>> searchAnunturi(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        // Trimitem același termen de căutare pentru ambele câmpuri (titlu și descriere)
        List<Anunt> rezultate = anuntRepository.findByTitluContainingIgnoreCaseOrDescriereContainingIgnoreCase(query, query);
        return ResponseEntity.ok(rezultate);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnunt(
            @PathVariable Long id,
            @RequestParam(required = false) Long adminUserId,
            Authentication auth
    ) {
        User user = null;
        if (adminUserId != null) {
            user = userRepository.findById(adminUserId).orElse(null);
        } else if (auth != null && auth.getName() != null) {
            user = userRepository.findByEmail(auth.getName()).orElse(null);
        }

        if (user != null && "ADMIN".equals(user.getRole())) {
            anuntRepository.deleteById(id);
            return ResponseEntity.ok("Anunț șters de administrator.");
        }

        return ResponseEntity.status(403).body("Nu ai permisiunea de a șterge acest anunț.");
    }
}