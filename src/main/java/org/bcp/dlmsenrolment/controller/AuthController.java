package org.bcp.dlmsenrolment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.LoginRequest;
import org.bcp.dlmsenrolment.dtos.respons.LoginResponse;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

/**
 * Authentication Controller
 * Handles user login and token generation via Keycloak OAuth2
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;
    
    @Value("${keycloak.token-url:http://localhost:8081/realms/master/protocol/openid-connect/token}")
    private String keycloakTokenUrl;
    
    @Value("${keycloak.client-id:Enrollement-Frontend}")
    private String keycloakClientId;
    
    @Value("${keycloak.client-secret:O6UAju5BmlJmwfphv8RCycFZ9G5JZFS2}")
    private String keycloakClientSecret;

    /**
     * Login endpoint - Authenticates user with Keycloak
     * Uses Resource Owner Password Credentials flow (Direct Access Grant)
     * 
     * @param loginRequest Contains username and password
     * @return JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.debug("Login attempt for user: {}", loginRequest.getUsername());
            
            // Validate input
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("VALIDATION_ERROR", "Username and password are required", null)
                );
            }

            // Call Keycloak token endpoint using Resource Owner Password Credentials flow
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", keycloakClientId);
            params.add("client_secret", keycloakClientSecret);
            params.add("grant_type", "password");
            params.add("username", loginRequest.getUsername());
            params.add("password", loginRequest.getPassword());
            params.add("scope", "openid profile email");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            try {
                // Call Keycloak
                var response = restTemplate.postForEntity(
                    keycloakTokenUrl,
                    new org.springframework.http.HttpEntity<>(params, headers),
                    Map.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> tokenResponse = response.getBody();
                    
                    String accessToken = (String) tokenResponse.get("access_token");
                    String refreshToken = (String) tokenResponse.get("refresh_token");
                    Long expiresIn = ((Number) tokenResponse.get("expires_in")).longValue();

                    log.info("User {} logged in successfully", loginRequest.getUsername());

                    // Return login response with token
                    LoginResponse loginResponse = LoginResponse.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(expiresIn)
                        .tokenType("Bearer")
                        .build();

                    return ResponseEntity.ok(
                        ApiResponse.success("Login successful", loginResponse)
                    );
                }
            } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
                log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
                return ResponseEntity.status(401).body(
                    ApiResponse.error("INVALID_CREDENTIALS", "Invalid username or password", null)
                );
            } catch (Exception e) {
                log.error("Error connecting to Keycloak: {}", e.getMessage());
                return ResponseEntity.status(503).body(
                    ApiResponse.error("SERVICE_UNAVAILABLE", "Authentication service unavailable", null)
                );
            }

            return ResponseEntity.status(401).body(
                ApiResponse.error("AUTH_FAILED", "Authentication failed", null)
            );

        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(500).body(
                ApiResponse.error("INTERNAL_ERROR", "Internal server error", null)
            );
        }
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.debug("User logout");
        return ResponseEntity.ok(
            ApiResponse.success("Logged out successfully", null)
        );
    }
}
