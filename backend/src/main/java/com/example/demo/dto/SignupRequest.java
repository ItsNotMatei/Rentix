package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    @NotBlank @Size(min = 2, max = 80)
    private String nume;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6, max = 100)
    private String password;
}
