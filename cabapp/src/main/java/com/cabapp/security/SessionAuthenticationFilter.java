package com.cabapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;

    public SessionAuthenticationFilter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String username = extractUsernameFromRequest(request);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (userDetails != null && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Log location permission status for debugging auto-coordinates
                    if (userDetails instanceof CustomUserPrincipal) {
                        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
                        log.debug("User {} authenticated. Location permission: {}, City: {}",
                                username, principal.hasLocationPermission(), principal.getCity());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractUsernameFromRequest(HttpServletRequest request) {
        // Extract username from session, header, or JWT token
        // This is a simplified version - you might use JWT tokens in production

        // Check session first
        String username = (String) request.getSession().getAttribute("username");
        if (username != null) {
            return username;
        }

        // Check custom header for API requests
        username = request.getHeader("X-User-ID");
        if (username != null) {
            return username;
        }

        // You could add JWT token extraction here
        // String token = extractJwtFromRequest(request);
        // if (token != null && jwtTokenUtil.validateToken(token)) {
        //     return jwtTokenUtil.getUsernameFromToken(token);
        // }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip authentication for public endpoints
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.equals("/api/locations/health") ||
                path.equals("/api/locations/service-area") ||
                path.startsWith("/ws/") ||  // WebSocket connections
                path.startsWith("/static/") ||
                path.startsWith("/favicon.ico");
    }
}