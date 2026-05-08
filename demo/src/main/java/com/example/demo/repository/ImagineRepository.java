package com.example.demo.repository;

import com.example.demo.model.ImagineAnunt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagineRepository extends JpaRepository<ImagineAnunt, Long> {
}