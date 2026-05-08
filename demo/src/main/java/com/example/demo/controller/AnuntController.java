package com.example.demo.controller;

import com.example.demo.model.Anunt;
import com.example.demo.model.User;
import com.example.demo.repository.AnuntRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/anunturi")
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
        // Luăm email-ul userului logat din sesiune
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        anunt.setUser(user); // Legăm anunțul de userul care l-a creat
        return anuntRepository.save(anunt);
    }
}