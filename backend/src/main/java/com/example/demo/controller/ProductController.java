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
            Product product = new Product();
            product.setTitlu(titlu);
            product.setDescriere(descriere);
            product.setAdresa(adresa);
            product.setTip(tip);
            product.setStatus("AVAILABLE");
            product.setUserId(userId);

            try {
                product.setPret(Double.parseDouble(pretStr));
            } catch (Exception e) {
                product.setPret(0.0);
            }

            Product savedProduct = productRepository.save(product);

            if (file != null && !file.isEmpty()) {
                String uploadDir = "uploads/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + uniqueFileName);
                Files.write(path, file.getBytes());

                // REPARARE AICI: În loc de setAnuntId, creăm un obiect "Anunt" fals,
                // îi setăm ID-ul salvat de la produs și îl trimitem către imagineAnunt.
                Anunt anuntDummy = new Anunt();
                anuntDummy.setId(savedProduct.getId());

                ImagineAnunt imagineAnunt = new ImagineAnunt();
                imagineAnunt.setUrl("http://localhost:8080/uploads/" + uniqueFileName);
                imagineAnunt.setAnunt(anuntDummy); // Folosim relația de tip obiect!

                imagineRepository.save(imagineAnunt);
            }

            return ResponseEntity.ok(savedProduct);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Eroare la salvarea fișierului pe disc: " + e.getMessage());
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