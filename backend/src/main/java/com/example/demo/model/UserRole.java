package com.example.demo.model;

public enum UserRole {
    USER,
    MODERATOR,
    ADMIN,
    SUPER_ADMIN;

    public static UserRole fromString(String role) {
        if (role == null || role.isBlank()) {
            return USER;
        }
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            if ("ADMIN".equalsIgnoreCase(role)) {
                return ADMIN;
            }
            return USER;
        }
    }

    public boolean isAtLeast(UserRole required) {
        return this.ordinal() >= required.ordinal();
    }
}
