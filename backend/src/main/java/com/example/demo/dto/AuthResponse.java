package com.example.demo.dto;

import com.example.demo.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final long expiresIn;
    private final UserPublicDto user;

    public static UserPublicDto toPublic(User user) {
        return UserPublicDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nume(user.getNume())
                .role(user.getRole())
                .isPro(user.isPro())
                .isVerified(user.isVerified())
                .profilePic(user.getProfilePic())
                .telefon(user.getTelefon())
                .adresa(user.getAdresa())
                .banned(user.isBanned())
                .suspended(user.isSuspended())
                .balance(user.getBalance())
                .build();
    }
}
