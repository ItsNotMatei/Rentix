package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Oprește blocarea cererilor POST (JSON)
                .authorizeHttpRequests(auth -> auth
                        // Permitem accesul la resurse statice (CSS, imagini)
                        .requestMatchers("/css/**", "/uploads/**", "/index.html", "/").permitAll()
                        // Permitem accesul la TOATE paginile din folderul sites (login, signup)
                        .requestMatchers("/sites/**").permitAll()
                        // Permitem accesul la endpoint-urile de inregistrare
                        .requestMatchers("/api/auth/**").permitAll()
                        // Orice altceva (ca "adauga-anunt") cere login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/sites/login.html") // Pagina ta de login
                        .loginProcessingUrl("/login")   // URL-ul intern de procesare
                        .defaultSuccessUrl("/index.html", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());
        return http.build();
    }
}