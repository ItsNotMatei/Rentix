package com.example.demo;

import com.example.demo.config.RentixDotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    public static void main(String[] args) {
        var dotenv = RentixDotenv.load();
        if (dotenv.get("RENTIX_DB_PASSWORD") != null) {
            System.out.println("[Rentix] .env încărcat — RENTIX_MAIL_ENABLED=" + dotenv.get("RENTIX_MAIL_ENABLED"));
        } else {
            System.err.println("[Rentix] .env negăsit — rulează din backend/ sau setează variabilele în mediu.");
        }
        SpringApplication.run(DemoApplication.class, args);
    }
}
