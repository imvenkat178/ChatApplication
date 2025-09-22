package com.cabapp.service;

import com.cabapp.model.User;
import com.cabapp.model.UserStatus;
import com.cabapp.model.PaymentMethod;
import com.cabapp.model.dto.LoginRequest;
import com.cabapp.model.dto.RegisterRequest;
import com.cabapp.model.dto.AuthResponse;
import com.cabapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       LocationService locationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationService = locationService;
    }

    // User login
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Find user by username (could be email or phone)
            User user = findUserByIdentifier(loginRequest.getUsername());

            if (user == null) {
                log.warn("Login attempt with unknown username: {}", loginRequest.getUsername());
                return AuthResponse.failure("Invalid username or password");
            }

            // Check password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                log.warn("Invalid password for user: {}", loginRequest.getUsername());
                return AuthResponse.failure("Invalid username or password");
            }

            // Check if account is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("Login attempt for inactive user: {}", loginRequest.getUsername());
                return AuthResponse.failure("Account is not active. Please contact support.");
            }

            // Update last login time
            user.updateLastLogin();

            // Update location permission if provided
            if (loginRequest.getLocationPermissionGranted() != null) {
                user.setLocationPermissionGranted(loginRequest.getLocationPermissionGranted());
            }

            userRepository.save(user);

            // Validate location data if provided
            if (loginRequest.hasLocationData() && loginRequest.isValidLocationData()) {
                if (loginRequest.isInAtlantaArea()) {
                    log.info("User {} logged in from Atlanta area: {}, {}",
                            user.getUsername(), loginRequest.getLatitude(), loginRequest.getLongitude());
                } else {
                    log.warn("User {} logged in from outside Atlanta service area: {}, {}",
                            user.getUsername(), loginRequest.getLatitude(), loginRequest.getLongitude());
                }
            }

            log.info("User {} logged in successfully. Location permission: {}",
                    user.getUsername(), user.isLocationPermissionEnabled());

            return AuthResponse.success(
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    user.getEmail(),
                    user.isLocationPermissionEnabled(),
                    user.getCity(),
                    user.getPreferredPaymentMethod() != null ? user.getPreferredPaymentMethod().name() : "CASH"
            );

        } catch (Exception e) {
            log.error("Error during login for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return AuthResponse.failure("Login failed. Please try again.");
        }
    }

    // User registration
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            // Set default values
            registerRequest.setDefaultValues();

            // Validate unique constraints
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return AuthResponse.failure("Username already exists");
            }

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return AuthResponse.failure("Email already registered");
            }

            if (userRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
                return AuthResponse.failure("Phone number already registered");
            }

            // Create new user
            User user = User.builder()
                    .id(generateUserId())
                    .username(registerRequest.getUsername())
                    .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                    .name(registerRequest.getName())
                    .email(registerRequest.getEmail())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .status(UserStatus.ACTIVE)
                    .city(registerRequest.getCity())
                    .state(registerRequest.getState())
                    .country(registerRequest.getCountry())
                    .locationPermissionGranted(registerRequest.getLocationPermissionGranted())
                    .preferredPaymentMethod(PaymentMethod.valueOf(registerRequest.getPreferredPaymentMethod()))
                    .preferredDeviceType(registerRequest.getPreferredDeviceType())
                    .notificationsEnabled(registerRequest.getNotificationsEnabled())
                    .build();

            user.updateLastLogin(); // Set initial login time
            user = userRepository.save(user);

            log.info("New user registered: {} from {}, {}. Location permission: {}",
                    user.getUsername(), user.getCity(), user.getState(), user.isLocationPermissionEnabled());

            return AuthResponse.success(
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    user.getEmail(),
                    user.isLocationPermissionEnabled(),
                    user.getCity(),
                    user.getPreferredPaymentMethod().name()
            );

        } catch (Exception e) {
            log.error("Error during registration for user {}: {}", registerRequest.getUsername(), e.getMessage());
            return AuthResponse.failure("Registration failed. Please try again.");
        }
    }

    // Change password
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        try {
            User user = findUserByIdentifier(username);
            if (user == null) {
                return false;
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                log.warn("Invalid current password for user: {}", username);
                return false;
            }

            // Update password
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            log.info("Password changed for user: {}", username);
            return true;

        } catch (Exception e) {
            log.error("Error changing password for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    // Update location permission
    @Transactional
    public boolean updateLocationPermission(String username, boolean granted) {
        try {
            User user = findUserByIdentifier(username);
            if (user == null) {
                return false;
            }

            user.setLocationPermissionGranted(granted);
            userRepository.save(user);

            log.info("Location permission updated for user {}: {}", username, granted);
            return true;

        } catch (Exception e) {
            log.error("Error updating location permission for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    // Logout (invalidate session)
    public boolean logout(String username) {
        try {
            log.info("User {} logged out", username);
            // Here you could invalidate JWT tokens, clear session data, etc.
            return true;
        } catch (Exception e) {
            log.error("Error during logout for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    // Validate username and password without full login
    public boolean validateCredentials(String username, String password) {
        try {
            User user = findUserByIdentifier(username);
            return user != null &&
                    passwordEncoder.matches(password, user.getPasswordHash()) &&
                    user.getStatus() == UserStatus.ACTIVE;
        } catch (Exception e) {
            log.error("Error validating credentials for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    // Find user by username, email, or phone
    private User findUserByIdentifier(String identifier) {
        // Try username first
        User user = userRepository.findByUsername(identifier).orElse(null);
        if (user != null) return user;

        // Try email
        user = userRepository.findByEmail(identifier).orElse(null);
        if (user != null) return user;

        // Try phone number
        return userRepository.findByPhoneNumber(identifier).orElse(null);
    }

    // Generate unique user ID
    private String generateUserId() {
        return "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Check if user exists
    public boolean userExists(String identifier) {
        return findUserByIdentifier(identifier) != null;
    }

    // Get user info (for session validation)
    public User getUserInfo(String username) {
        return findUserByIdentifier(username);
    }

    // Account deactivation
    @Transactional
    public boolean deactivateAccount(String username) {
        try {
            User user = findUserByIdentifier(username);
            if (user == null) {
                return false;
            }

            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);

            log.info("Account deactivated for user: {}", username);
            return true;

        } catch (Exception e) {
            log.error("Error deactivating account for user {}: {}", username, e.getMessage());
            return false;
        }
    }
}