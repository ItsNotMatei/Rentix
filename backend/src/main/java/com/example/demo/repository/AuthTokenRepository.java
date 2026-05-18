package com.example.demo.repository;

import com.example.demo.model.AuthToken;
import com.example.demo.model.AuthTokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

    Optional<AuthToken> findByIdAndTypeAndUsedFalseAndExpiresAtAfter(
            String id, AuthTokenType type, LocalDateTime now);

    void deleteByUserIdAndType(Long userId, AuthTokenType type);
}
