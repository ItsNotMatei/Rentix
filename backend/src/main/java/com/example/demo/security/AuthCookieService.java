package com.example.demo.security;

import com.example.demo.dto.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "rentix_access";
    public static final String REFRESH_COOKIE = "rentix_refresh";

    private final JwtProperties jwtProperties;

    @Value("${rentix.app.production:false}")
    private boolean production;

    @Value("${rentix.auth.cookie-same-site:Lax}")
    private String sameSite;

    public void writeAuthCookies(HttpServletResponse response, AuthResponse auth) {
        long accessMaxAge = jwtProperties.getAccessExpirationMs() / 1000;
        long refreshMaxAge = jwtProperties.getRefreshExpirationMs() / 1000;
        response.addHeader("Set-Cookie", buildCookie(ACCESS_COOKIE, auth.getAccessToken(), "/", accessMaxAge).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_COOKIE, auth.getRefreshToken(), "/api/auth", refreshMaxAge).toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie(ACCESS_COOKIE, "", "/", 0).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_COOKIE, "", "/api/auth", 0).toString());
    }

    public String readAccessToken(HttpServletRequest request) {
        return readCookie(request, ACCESS_COOKIE);
    }

    public String readRefreshToken(HttpServletRequest request) {
        return readCookie(request, REFRESH_COOKIE);
    }

    private String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildCookie(String name, String value, String path, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(production)
                .sameSite(sameSite)
                .path(path)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
