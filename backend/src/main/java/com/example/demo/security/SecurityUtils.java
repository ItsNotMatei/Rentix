package com.example.demo.security;

import com.example.demo.model.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static RentixUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof RentixUserDetails details)) {
            throw new AccessDeniedException("Autentificare necesară.");
        }
        return details;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static UserRole currentRole() {
        return currentUser().getRole();
    }

    public static void requireRole(UserRole minimum) {
        if (!currentRole().isAtLeast(minimum)) {
            throw new AccessDeniedException("Nu ai permisiuni suficiente.");
        }
    }
}
