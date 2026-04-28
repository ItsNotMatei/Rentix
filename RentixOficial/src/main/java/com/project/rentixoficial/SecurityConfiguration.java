package com.project.rentixoficial;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // IMPORTA ASTA
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTA ASTA

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Dezactivăm CSRF pentru simplitate în dezvoltare (pentru API-uri)
                .authorizeHttpRequests(auth -> auth
                        // Permitem accesul liber la resursele statice generate de React
                        .requestMatchers("/", "/index.html", "/assets/**", "/*.js", "/*.css", "/favicon.ico").permitAll()
                        // Permitem accesul liber la rutele de React și înregistrare
                        .requestMatchers("/home", "/login", "/register/**").permitAll()
                        // Orice altceva necesită autentificare
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Pagina ta custom de login
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}