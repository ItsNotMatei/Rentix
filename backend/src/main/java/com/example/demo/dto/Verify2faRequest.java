package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Verify2faRequest {
    @NotBlank
    private String challengeId;
    @NotBlank
    @Size(min = 6, max = 6)
    private String code;
}
