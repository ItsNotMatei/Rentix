package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResultDto {
    private final boolean requiresTwoFactor;
    private final String challengeId;
    private final String message;
    private final AuthResponse auth;
    private AuthResponse authResponse;
}
