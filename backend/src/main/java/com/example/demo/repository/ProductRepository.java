package com.example.demo.repository; // Verifică să fie pachetul tău corect

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository aduce automat metodele findById, save, delete etc.
}