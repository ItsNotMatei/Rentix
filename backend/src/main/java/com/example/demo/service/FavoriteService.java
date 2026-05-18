package com.example.demo.service;

import com.example.demo.model.Favorite;
import com.example.demo.model.ImagineAnunt;
import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.ImagineRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private static final String FALLBACK_IMAGE =
            "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=600";

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ImagineRepository imagineRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    public List<Map<String, Object>> listForUser(Long userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ImagineAnunt> images = imagineRepository.findAll();
        return favorites.stream()
                .map(f -> productRepository.findById(f.getProductId()).orElse(null))
                .filter(Objects::nonNull)
                .map(p -> toProductMap(p, images))
                .collect(Collectors.toList());
    }

    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public boolean toggle(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Produsul nu există.");
        }
        Optional<Favorite> existing = favoriteRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favoriteRepository.save(favorite);
        return true;
    }

    private Map<String, Object> toProductMap(Product p, List<ImagineAnunt> images) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", p.getId());
        productMap.put("titlu", p.getTitlu());
        productMap.put("pret", p.getPret());
        productMap.put("descriere", p.getDescriere());
        productMap.put("adresa", p.getAdresa());
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

        return productMap;
    }
}
