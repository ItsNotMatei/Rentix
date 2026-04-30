package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Permite cererile POST fără token
                .authorizeHttpRequests(auth -> auth
                        // Deschidem tot ce e legat de pagini și stiluri
                        .requestMatchers("/", "/index.html", "/css/**", "/sites/**").permitAll()
                        // Deschidem API-ul de înregistrare
                        .requestMatchers("/api/auth/**").permitAll()
                        // Doar restul cererilor (ca adăugarea de anunțuri) cer login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/sites/login.html") // Unde e formularul de login
                        .loginProcessingUrl("/login")   // URL-ul pe care îl apelează formularul
                        .defaultSuccessUrl("/index.html", true) // Unde te trimite după ce reușești
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/index.html")
                        .permitAll()
                );

        return http.build();
    }
}