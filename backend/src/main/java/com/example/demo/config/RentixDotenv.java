package com.example.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Încarcă backend/.env sau .env din directorul curent în System properties.
 */
public final class RentixDotenv {

    private RentixDotenv() {
    }

    public static Dotenv load() {
        Dotenv dotenv = resolveDotenv();
        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }
        return dotenv;
    }

    private static Dotenv resolveDotenv() {
        if (Files.isRegularFile(Path.of("backend", ".env"))) {
            return Dotenv.configure().directory("./backend").ignoreIfMissing().load();
        }
        return Dotenv.configure().ignoreIfMissing().load();
    }
}
