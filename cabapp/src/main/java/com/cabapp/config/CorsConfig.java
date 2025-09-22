package com.cabapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * CORS Configuration to allow React frontend access
 * 
 * Allows:
 * - React development server (localhost:3000)
 * - Production frontend domains
 * - WebSocket connections
 * - All HTTP methods for API endpoints
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Global CORS configuration for REST endpoints
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // All API endpoints
                .allowedOriginPatterns("*")  // Allow all origins for development
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight requests for 1 hour
        
        // WebSocket endpoints
        registry.addMapping("/ws/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true);
    }

    /**
     * More detailed CORS configuration bean for specific requirements
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow React development and production URLs
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",    // React development server
            "http://localhost:3001",    // Alternative React port
            "https://*.vercel.app",     // Vercel deployments
            "https://*.netlify.app"     // Netlify deployments
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}