package com.example.demo.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConfigValidator {

    private final Environment environment;

    public DatabaseConfigValidator(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logDatabaseConfigSource() {
        String fromEnv = environment.getProperty("RENTIX_DB_PASSWORD");
        String password = environment.getProperty("spring.datasource.password");

        if (fromEnv == null || fromEnv.isBlank()) {
            System.err.println("[Rentix] RENTIX_DB_PASSWORD lipsește. Creează backend/.env (vezi .env.example).");
            return;
        }
        if (password == null || password.isBlank()) {
            System.err.println("[Rentix] spring.datasource.password este gol — verifică ${RENTIX_DB_PASSWORD} în application.properties.");
            return;
        }
        if (fromEnv != null && !fromEnv.isBlank()) {
            System.out.println("[Rentix] RENTIX_DB_PASSWORD încărcată din .env");
        }
    }
}
