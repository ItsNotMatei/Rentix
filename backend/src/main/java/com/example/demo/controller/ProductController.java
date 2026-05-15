package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.ImagineAnunt;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ImagineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173") // Meciul perfect cu portul tău din Vite/React
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImagineRepository imagineRepository;

    // 1. SALVAREA ANUNTULUI SI A IMAGINII DIN DISPOZITIV
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam("titlu") String titlu,
            @RequestParam("pret") String pretStr,
            @RequestParam("descriere") String descriere,
            @RequestParam("adresa") String adresa,
            @RequestParam("tip") String tip,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "imagine", required = false) MultipartFile file) {

        try {
            // Pasul A: Salvăm datele text în tabela 'anunturi'
            Product product = new Product();
            product.setTitlu(titlu);
            product.setDescriere(descriere);
            product.setAdresa(adresa);
            product.setTip(tip);

            // CORECTARE EROARE: Schimbat din "valabil" în "AVAILABLE" sau valoarea trimisă din formular
            // pentru a se potrivi perfect cu structura coloanei de tip Enum/String din baza de date
            product.setStatus("AVAILABLE");
            product.setUserId(userId);

            try {
                product.setPret(Double.parseDouble(pretStr));
            } catch (Exception e) {
                product.setPret(0.0);
            }

            // Salvarea generează ID-ul automat în MySQL
            Product savedProduct = productRepository.save(product);

            // Pasul B: Dacă utilizatorul a selectat o imagine din dispozitiv, o salvăm fizic
            if (file != null && !file.isEmpty()) {
                String uploadDir = "uploads/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs(); // Creează folderul fizic 'uploads' dacă nu există
                }

                // Generăm un nume unic fișierului pentru a evita suprascrierea pe disc
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + uniqueFileName);
                Files.write(path, file.getBytes());

                // Pasul C: Salvăm legătura în tabela 'imagine_anunt' folosind modelul tău 'ImagineAnunt'
                ImagineAnunt imagineAnunt = new ImagineAnunt();
                imagineAnunt.setAnuntId(savedProduct.getId());
                imagineAnunt.setUrl("http://localhost:8080/uploads/" + uniqueFileName);

                imagineRepository.save(imagineAnunt);
            }

            return ResponseEntity.ok(savedProduct);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Eroare la salvarea fișierului pe disc: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Eroare la scrierea în baza de date MySQL: " + e.getMessage());
        }
    }

    // 2. RETURNAREA PRODUSELOR CU TOT CU POZĂ PENTRU PAGINA HOME (Stil E-commerce)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        // Preluăm toate imaginile o singură dată din DB pentru a optimiza performanța
        List<ImagineAnunt> toateImaginile = imagineRepository.findAll();

        for (Product p : products) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", p.getId());
            productMap.put("titlu", p.getTitlu());
            productMap.put("pret", p.getPret());
            productMap.put("descriere", p.getDescriere());
            productMap.put("adresa", p.getAdresa());
            productMap.put("tip", p.getTip());
            productMap.put("status", p.getStatus());
            productMap.put("userId", p.getUserId());

            // Mapăm o listă goală de recenzii dacă nu sunt încă definite, pentru ca Home.jsx (reviews.length) să nu dea crash
            productMap.put("reviews", new ArrayList<>());

            // Căutăm URL-ul imaginii asociate produsului curent
            Optional<ImagineAnunt> img = toateImaginile.stream()
                    .filter(i -> i.getAnuntId() != null && i.getAnuntId().equals(p.getId()))
                    .findFirst();

            if (img.isPresent()) {
                productMap.put("imageUrl", img.get().getUrl());
            } else {
                // Imagine modernă tip placeholder din Unsplash dacă nu există poză încărcată
                productMap.put("imageUrl", "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=600");
            }

            response.add(productMap);
        }
        return ResponseEntity.ok(response);
    }

    // 3. RETURNAREA UNUI SINGUR PRODUS PENTRU PAGINA DE DETALII
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Produsul nu a fost găsit în baza de date.");
        }

        Product p = productOpt.get();
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", p.getId());
        productMap.put("titlu", p.getTitlu());
        productMap.put("pret", p.getPret());
        productMap.put("descriere", p.getDescriere());
        productMap.put("adresa", p.getAdresa());
        productMap.put("tip", p.getTip());
        productMap.put("status", p.getStatus());
        productMap.put("userId", p.getUserId());
        productMap.put("reviews", new ArrayList<>());

        Optional<ImagineAnunt> img = imagineRepository.findAll().stream()
                .filter(i -> i.getAnuntId() != null && i.getAnuntId().equals(p.getId()))
                .findFirst();

        if (img.isPresent()) {
            productMap.put("imageUrl", img.get().getUrl());
        } else {
            productMap.put("imageUrl", "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=600");
        }

        return ResponseEntity.ok(productMap);
    }
}