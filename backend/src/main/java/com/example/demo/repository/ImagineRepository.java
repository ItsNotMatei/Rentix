package com.example.demo.repository;

import com.example.demo.model.ImagineAnunt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImagineRepository extends JpaRepository<ImagineAnunt, Long> {
    Optional<ImagineAnunt> findByAnuntId(Long anuntId);
}