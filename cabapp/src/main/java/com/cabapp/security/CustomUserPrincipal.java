package com.cabapp.security;

import com.cabapp.model.User;
import com.cabapp.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {

    private String id;
    private String username;
    private String email;
    private String name;
    private String passwordHash;
    private UserStatus status;
    private Boolean locationPermissionGranted;
    private String city;
    private String preferredPaymentMethod;

    public static CustomUserPrincipal create(User user) {
        return new CustomUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getPasswordHash(),
                user.getStatus(),
                user.getLocationPermissionGranted(),
                user.getCity(),
                user.getPreferredPaymentMethod() != null ? user.getPreferredPaymentMethod().name() : "CASH"
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, all users have ROLE_USER
        // You can extend this to support ROLE_ADMIN, ROLE_DRIVER, etc.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // Custom methods for cab booking system
    public boolean hasLocationPermission() {
        return locationPermissionGranted != null && locationPermissionGranted;
    }

    public boolean isInAtlantaArea() {
        return city != null &&
                (city.toLowerCase().contains("atlanta") ||
                        city.toLowerCase().contains("sandy springs") ||
                        city.toLowerCase().contains("marietta") ||
                        city.toLowerCase().contains("alpharetta"));
    }

    public boolean prefersCashPayment() {
        return "CASH".equals(preferredPaymentMethod);
    }
}