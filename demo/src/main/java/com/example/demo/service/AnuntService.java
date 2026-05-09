package com.example.demo.service;

import com.example.demo.model.Anunt;
import com.example.demo.model.User;
import com.example.demo.repository.AnuntRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnuntService {

    private final AnuntRepository anuntRepo;
    private final UserRepository userRepo;

    public AnuntService(AnuntRepository anuntRepo, UserRepository userRepo) {
        this.anuntRepo = anuntRepo;
        this.userRepo = userRepo;
    }

    public List<Anunt> getAll() {
        return anuntRepo.findAll();
    }

    public Anunt create(Anunt anunt, String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        anunt.setUser(user);
        return anuntRepo.save(anunt);
    }

    public Anunt getById(Long id) {
        return anuntRepo.findById(id).orElseThrow();
    }
}