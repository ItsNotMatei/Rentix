package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final RentixUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> session = accessor.getSessionAttributes();

            if (session == null || session.get("userId") == null) {
                throw new AccessDeniedException("Conexiune WebSocket neautorizată. Lipsesc credențialele.");
            }

            String email = (String) session.get("email");
            try {
                RentixUserDetails user = (RentixUserDetails) userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                accessor.setUser(auth);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                throw new AccessDeniedException("Eroare la autentificarea sesiunii WebSocket.");
            }
        }
        return message;
    }
}

