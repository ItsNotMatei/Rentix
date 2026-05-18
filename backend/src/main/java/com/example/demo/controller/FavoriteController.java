package com.example.demo.controller;

import com.example.demo.security.SecurityUtils;
import com.example.demo.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listMine() {
        Long userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(favoriteService.listForUser(userId));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> check(@PathVariable Long productId) {
        Long userId = SecurityUtils.currentUserId();
        boolean favorited = favoriteService.isFavorite(userId, productId);
        return ResponseEntity.ok(Map.of("favorited", favorited));
    }

    @PostMapping("/toggle/{productId}")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long productId) {
        Long userId = SecurityUtils.currentUserId();
        boolean favorited = favoriteService.toggle(userId, productId);
        return ResponseEntity.ok(Map.of("favorited", favorited, "productId", productId));
    }
}
