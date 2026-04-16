package org.bcp.dlmsenrolment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.bcp.dlmsenrolment.config.RequestLoggingFilter;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * SecurityConfig - Main Spring Security configuration.
 * 
 * ARCHITECTURE:
 * ─────────────
 * 1. OAuth2 Resource Server: Validates JWT tokens from Keycloak
 * 2. Custom JwtAuthenticationFilter: Extracts token from request header
 * 3. Custom PermissionAuthorizationFilter: Enforces RBAC permissions
 * 4. Stateless session: No cookies, no session state
 * 5. CORS enabled: For React frontend
 * 6. CSRF disabled: Stateless API doesn't need CSRF protection
 * 
 * FILTER CHAIN ORDER:
 * ──────────────────
 * 1. CorsFilter (if enabled)
 * 2. JwtAuthenticationFilter (extract & validate JWT)
 * 3. PermissionAuthorizationFilter (check RBAC permissions)
 * 4. OAuth2 filters (Spring's default OAuth2 handling)
 * 5. FilterSecurityInterceptor (final check)
 * 
 * PUBLIC ENDPOINTS (permit-all):
 * ──────────────────────────────
 * - /swagger-ui/** (API documentation)
 * - /v3/api-docs/** (OpenAPI specs)
 * - /public/** (explicitly public endpoints)
 * - /health (health check)
 * 
 * PROTECTED ENDPOINTS:
 * ───────────────────
 * - Everything else requires AUTHENTICATION
 * - Permissions checked by PermissionAuthorizationFilter
 * - 403 Forbidden if user lacks permission
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final RequestLoggingFilter requestLoggingFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PermissionAuthorizationFilter permissionAuthorizationFilter;

    /**
     * Whitelisted endpoints that don't require authentication
     * These are public endpoints (Swagger, health, etc.)
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/auth/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/v1/api-docs/**",
        "/public/**",
        "/health",
        "/actuator/**",
        "/swagger-resources/**",
        "/configuration/ui",
        "/configuration/security",
        "/swagger-ui.html",
        "/webjars/**"
    };

    /**
     * Main Security Filter Chain
     * 
     * Configuration:
     * 1. OAuth2 Resource Server: Validates JWT with Keycloak
     * 2. Authorization: Endpoints require authentication (custom logic)
     * 3. Session: Stateless (no cookies, no sessions)
     * 4. CORS: Enabled for frontend
     * 5. CSRF: Disabled (stateless API)
     * 6. Filters: Custom JWT and Permission filters added
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ===== AUTHORIZATION =====
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // Health endpoint
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // ===== SESSION MANAGEMENT =====
                // Stateless: No session cookies, no session state
                // Each request must include JWT token
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ===== OAUTH2 RESOURCE SERVER =====
                // Validates JWT tokens using Keycloak's public key
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults())
                )

                // ===== CORS =====
                // Enable CORS for React frontend
                .cors(withDefaults())

                // ===== CSRF =====
                // Disabled for stateless API (no session = no CSRF risk)
                .csrf(csrf -> csrf.disable())

                // ===== CUSTOM FILTERS =====
                // Add our custom filters to the chain
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(permissionAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration - Allow React frontend to make requests
     * 
     * This allows:
     * - Origin: http://localhost:3000 (React frontend)
     * - Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
     * - Headers: Authorization, Content-Type, etc.
     * - Credentials: Include cookies if needed
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // TODO: Change in production to your actual frontend URL
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",      // Vite React dev server
            "http://localhost:3000",      // Local React dev server (Create React App)
            "http://localhost:3001",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Request-Id",
            "X-Requested-With",
            "Accept",
            "Access-Control-Allow-Origin"
        ));

        configuration.setExposedHeaders(Arrays.asList(
            "X-Request-Id"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

