package com.example.demo.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rentix.jwt")
public class JwtProperties {
    private String secret;
    private long accessExpirationMs = 900_000;
    private long refreshExpirationMs = 604_800_000;
}
