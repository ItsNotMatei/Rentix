package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPublicDto {
    private Long id;
    private String email;
    private String nume;
    private String role;
    private boolean isPro;
    private boolean isVerified;
    private String profilePic;
    private String telefon;
    private String adresa;
    private boolean banned;
    private boolean suspended;
}
