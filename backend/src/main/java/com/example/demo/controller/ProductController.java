package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.model.ImagineAnunt;
import com.example.demo.model.Anunt; // Asigură-te că importul ăsta există
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
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImagineRepository imagineRepository;

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> payload) {
        try {
            // Extragem datele din JSON-ul trimis de React
            String titlu = (String) payload.get("titlu");
            String descriere = (String) payload.get("descriere");
            String adresa = (String) payload.get("adresa");
            String tip = (String) payload.get("tip");
            String status = (String) payload.get("status");
            Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : 2L;
            String imagineUrl = (String) payload.get("imagineUrl"); // Link-ul primit de la Cloudinary

            Double pret;
            try {
                pret = Double.parseDouble(payload.get("pret").toString());
            } catch (Exception e) {
                pret = 0.0;
            }

            // Construim și salvăm produsul
            Product product = new Product();
            product.setTitlu(titlu);
            product.setDescriere(descriere);
            product.setAdresa(adresa);
            product.setTip(tip);
            product.setStatus(status != null ? status : "AVAILABLE");
            product.setUserId(userId);
            product.setPret(pret);

            Product savedProduct = productRepository.save(product);

            // Dacă React a încărcat cu succes o imagine în Cloudinary, o salvăm în tabelă
            if (imagineUrl != null && !imagineUrl.trim().isEmpty()) {
                Anunt anuntDummy = new Anunt();
                anuntDummy.setId(savedProduct.getId());

                ImagineAnunt imagineAnunt = new ImagineAnunt();
                imagineAnunt.setUrl(imagineUrl); // Salvăm direct URL-ul securizat de Cloudinary (https://res.cloudinary.com/...)
                imagineAnunt.setAnunt(anuntDummy);

                imagineRepository.save(imagineAnunt);
            }

            return ResponseEntity.ok(savedProduct);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Eroare la scrierea în baza de date MySQL: " + e.getMessage());
        }
    }


    // 2. RETURNAREA PRODUSELOR CU TOT CU POZĂ PENTRU PAGINA HOME
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

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
            productMap.put("reviews", new ArrayList<>());

            // REPARARE AICI: În loc de i.getAnuntId(), trecem prin i.getAnunt().getId()
            Optional<ImagineAnunt> img = toateImaginile.stream()
                    .filter(i -> i.getAnunt() != null && i.getAnunt().getId().equals(p.getId()))
                    .findFirst();

            if (img.isPresent()) {
                productMap.put("imageUrl", img.get().getUrl());
            } else {
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

        // REPARARE AICI: În loc de i.getAnuntId(), trecem prin i.getAnunt().getId()
        Optional<ImagineAnunt> img = imagineRepository.findAll().stream()
                .filter(i -> i.getAnunt() != null && i.getAnunt().getId().equals(p.getId()))
                .findFirst();

        if (img.isPresent()) {
            productMap.put("imageUrl", img.get().getUrl());
        } else {
            productMap.put("imageUrl", "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=600");
        }

        return ResponseEntity.ok(productMap);
    }
}