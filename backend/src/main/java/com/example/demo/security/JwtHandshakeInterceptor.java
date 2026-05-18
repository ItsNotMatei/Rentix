package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final AuthCookieService authCookieService;
    private final JwtService jwtService;
    private final RentixUserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return true;
        }
        var http = servletRequest.getServletRequest();
        String token = authCookieService.readAccessToken(http);
        if (token == null) {
            String param = http.getParameter("token");
            if (param != null && !param.isBlank()) {
                token = param;
            }
        }
        if (token == null || !jwtService.isAccessToken(token)) {
            return true;
        }
        try {
            String email = jwtService.extractEmail(token);
            RentixUserDetails user = (RentixUserDetails) userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, user.getUsername())) {
                attributes.put("userId", user.getId());
                attributes.put("email", email);
            }
            return true;
        } catch (Exception e) {
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
