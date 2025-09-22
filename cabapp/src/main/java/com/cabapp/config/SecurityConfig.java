package com.cabapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration for Basic Authentication
 * 
 * Features:
 * - BCrypt password encryption
 * - Session-based authentication (no JWT)
 * - HTTP sessions for login state
 * - Basic username/password login
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * Password encoder using BCrypt for secure password hashing
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for good security
    }
    
    /**
     * Security filter chain configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Session management - use HTTP sessions
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                       .maximumSessions(3) // Allow 3 concurrent sessions per user
                       .maxSessionsPreventsLogin(false))
            
            // URL authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/").permitAll()
                
                // Protected endpoints (authentication required)
                .requestMatchers("/api/rides/**").authenticated()
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/locations/**").authenticated()
                .requestMatchers("/api/drivers/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated())
            
            // Disable default form login (we'll use custom API endpoints)
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // CSRF configuration
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/ws/**", "/h2-console/**")
                .disable())
            
            // CORS configuration
            .cors(cors -> cors.configure(http))
            
            // Frame options for H2 console
            .headers(headers -> headers
                .frameOptions().sameOrigin())
            
            // Session fixation protection
            .sessionManagement(session -> session
                .sessionFixation().changeSessionId());
        
        return http.build();
    }
}