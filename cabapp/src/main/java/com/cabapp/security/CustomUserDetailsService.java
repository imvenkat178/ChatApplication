package com.cabapp.security;

import com.cabapp.model.User;
import com.cabapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // Update last login time when user details are loaded for authentication
        user.updateLastLogin();
        userRepository.save(user);

        log.info("User {} loaded for authentication. Location permission: {}",
                username, user.isLocationPermissionEnabled());

        return CustomUserPrincipal.create(user);
    }

    // Load user by ID (for JWT token validation)
    public UserDetails loadUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        log.debug("User {} loaded by ID for token validation", user.getUsername());

        return CustomUserPrincipal.create(user);
    }

    // Check if user has location permission (for auto-coordinates)
    public boolean hasLocationPermission(String username) {
        return userRepository.findByUsername(username)
                .map(User::isLocationPermissionEnabled)
                .orElse(false);
    }

    // Update user's location permission
    public void updateLocationPermission(String username, boolean granted) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLocationPermissionGranted(granted);
            userRepository.save(user);
            log.info("Updated location permission for user {}: {}", username, granted);
        });
    }
}