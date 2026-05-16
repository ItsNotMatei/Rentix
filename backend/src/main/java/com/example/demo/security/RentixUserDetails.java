package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class RentixUserDetails implements UserDetails {

    private final User user;

    public RentixUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        UserRole role = UserRole.fromString(user.getRole());
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.isBanned();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (user.isBanned()) {
            return false;
        }
        if (user.isSuspended()) {
            if (user.getSuspendedUntil() == null) {
                return false;
            }
            return java.time.LocalDateTime.now().isAfter(user.getSuspendedUntil());
        }
        return true;
    }

    public Long getId() {
        return user.getId();
    }

    public UserRole getRole() {
        return UserRole.fromString(user.getRole());
    }
}
