package com.example.demo.controller;

import com.example.demo.model.Anunt;
import com.example.demo.model.ImagineAnunt;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.repository.ImagineRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtils;
import com.example.demo.service.GeocodingService;
import com.example.demo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImagineRepository imagineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private GeocodingService geocodingService;

    private static final String FALLBACK_IMAGE =
            "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=600";

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Map<String, Object> payload) {
        try {
            String titlu = (String) payload.get("titlu");
            String descriere = (String) payload.get("descriere");
            String adresa = (String) payload.get("adresa");
            String tip = (String) payload.get("tip");
            String status = (String) payload.get("status");
            String categorie = (String) payload.get("categorie");
            String stareProdus = (String) payload.get("stareProdus");
            if (stareProdus == null) {
                stareProdus = (String) payload.get("stare");
            }
            Long userId = SecurityUtils.currentUserId();
            String imagineUrl = (String) payload.get("imagineUrl");

            Double pret;
            try {
                pret = Double.parseDouble(payload.get("pret").toString());
            } catch (Exception e) {
                pret = 0.0;
            }

            Product product = new Product();
            product.setTitlu(titlu);
            product.setDescriere(descriere);
            product.setAdresa(adresa);
            product.setTip(tip);
            product.setStatus(status != null ? status : "AVAILABLE");
            product.setCategorie(categorie);
            product.setStareProdus(stareProdus);
            product.setUserId(userId);
            product.setPret(pret);

            geocodingService.geocodeAddress(adresa).ifPresent(coords -> {
                product.setLatitude(coords[0]);
                product.setLongitude(coords[1]);
            });

            Product savedProduct = productRepository.save(product);

            saveImages(savedProduct.getId(), payload);

            return ResponseEntity.ok(toProductMap(savedProduct, imagineRepository.findAll()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Eroare la scrierea în baza de date MySQL: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ImagineAnunt> images = imagineRepository.findAll();
        return ResponseEntity.ok(products.stream()
                .map(p -> toProductMap(p, images))
                .collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<ImagineAnunt> images = imagineRepository.findAll();
        List<Map<String, Object>> results = productRepository.findAll().stream()
                .filter(p -> contains(p.getTitlu(), q)
                        || contains(p.getDescriere(), q)
                        || contains(p.getAdresa(), q)
                        || contains(p.getCategorie(), q))
                .map(p -> toProductMap(p, images))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<Map<String, Object>>> byCategory(@RequestParam String categorie) {
        String cat = categorie == null ? "" : categorie.trim();
        if (cat.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<ImagineAnunt> images = imagineRepository.findAll();
        List<Map<String, Object>> results = productRepository.findAll().stream()
                .filter(p -> p.getCategorie() != null && p.getCategorie().equalsIgnoreCase(cat))
                .map(p -> toProductMap(p, images))
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/meta/categories")
    public ResponseEntity<List<String>> listCategories() {
        return ResponseEntity.ok(List.of(
                "Haine", "Gadgeturi", "Scule", "Sport", "Console", "Auto", "Evenimente", "Casă & grădină", "Altele"
        ));
    }

    @GetMapping("/meta/conditions")
    public ResponseEntity<List<Map<String, String>>> listConditions() {
        return ResponseEntity.ok(List.of(
                Map.of("value", "NOU", "label", "Nou"),
                Map.of("value", "CA_NOU", "label", "Ca nou"),
                Map.of("value", "PUTIN_FOLOSIT", "label", "Puțin folosit"),
                Map.of("value", "FOLOSIT", "label", "Folosit"),
                Map.of("value", "UZAT", "label", "Uzat / defecte minore")
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Produsul nu a fost găsit în baza de date.");
        }
        return ResponseEntity.ok(toProductMap(productOpt.get(), imagineRepository.findAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product product = productOpt.get();
        Long currentId = SecurityUtils.currentUserId();
        UserRole role = SecurityUtils.currentRole();
        boolean isOwner = product.getUserId() != null && product.getUserId().equals(currentId);
        if (!isOwner && !role.isAtLeast(UserRole.MODERATOR)) {
            return ResponseEntity.status(403).body("Nu ai permisiunea să ștergi acest anunț.");
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok("Anunț șters.");
    }

    @SuppressWarnings("unchecked")
    private void saveImages(Long productId, Map<String, Object> payload) {
        List<String> urls = new ArrayList<>();
        Object multi = payload.get("imagineUrls");
        if (multi instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) {
                    urls.add(item.toString());
                }
            }
        }
        String single = (String) payload.get("imagineUrl");
        if (single != null && !single.isBlank()) {
            urls.add(0, single);
        }
        if (urls.isEmpty()) return;

        Anunt anuntRef = new Anunt();
        anuntRef.setId(productId);
        for (String url : urls) {
            ImagineAnunt imagineAnunt = new ImagineAnunt();
            imagineAnunt.setUrl(url);
            imagineAnunt.setAnunt(anuntRef);
            imagineRepository.save(imagineAnunt);
        }
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private Map<String, Object> toProductMap(Product p, List<ImagineAnunt> images) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", p.getId());
        productMap.put("titlu", p.getTitlu());
        productMap.put("pret", p.getPret());
        productMap.put("descriere", p.getDescriere());
        productMap.put("adresa", p.getAdresa());
        productMap.put("latitude", p.getLatitude());
        productMap.put("longitude", p.getLongitude());
        productMap.put("tip", p.getTip());
        productMap.put("categorie", p.getCategorie());
        productMap.put("stareProdus", p.getStareProdus());
        productMap.put("status", p.getStatus());
        productMap.put("userId", p.getUserId());

        List<String> imageUrls = images.stream()
                .filter(i -> i.getAnunt() != null && i.getAnunt().getId().equals(p.getId()))
                .map(ImagineAnunt::getUrl)
                .collect(Collectors.toList());
        productMap.put("images", imageUrls);
        productMap.put("imageUrl", imageUrls.isEmpty() ? FALLBACK_IMAGE : imageUrls.get(0));

        User owner = p.getUserId() != null ? userRepository.findById(p.getUserId()).orElse(null) : null;
        String ownerName = owner != null && owner.getNume() != null && !owner.getNume().isBlank()
                ? owner.getNume()
                : "Proprietar";
        productMap.put("ownerName", ownerName);
        productMap.put("ownerVerified", owner != null && owner.isVerified());

        Map<String, Object> userObj = new HashMap<>();
        userObj.put("id", p.getUserId());
        userObj.put("nume", ownerName);
        userObj.put("isVerified", owner != null && owner.isVerified());
        userObj.put("verified", owner != null && owner.isVerified());
        productMap.put("user", userObj);

        Map<String, Object> stats = reviewService.getReviewStats(p.getId());
        productMap.put("averageRating", stats.get("averageRating"));
        productMap.put("reviewCount", stats.get("reviewCount"));
        productMap.put("reviews", reviewService.getReviewDtos(p.getId()));

        return productMap;
    }
}
