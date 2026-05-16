package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rentix.stripe")
public class StripeProperties {
    private String secretKey = "";
    private String webhookSecret = "";
    private String publishableKey = "";
}
