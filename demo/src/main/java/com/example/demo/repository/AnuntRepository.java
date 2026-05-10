package com.example.demo.repository;

import com.example.demo.model.Anunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AnuntRepository extends JpaRepository<Anunt, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Anunt a WHERE a.id = :id")
    Optional<Anunt> findByIdForUpdate(Long id);
}