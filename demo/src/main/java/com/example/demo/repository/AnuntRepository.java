package com.example.demo.repository;

import com.example.demo.model.Anunt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnuntRepository extends JpaRepository<Anunt, Long> {
}