package org.bcp.dlmsenrolment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.LoginRequest;
import org.bcp.dlmsenrolment.dtos.respons.LoginResponse;
import org.bcp.dlmsenrolment.entities.User;
import org.bcp.dlmsenrolment.entities.UserRole;
import org.bcp.dlmsenrolment.excptions.ApiResponse;
import org.bcp.dlmsenrolment.repositories.UserRepository;
import org.bcp.dlmsenrolment.repositories.UserRoleRepository;
import org.bcp.dlmsenrolment.services.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
    * AuthController - Handles Keycloak-based login and local user synchronization.
 *
 * FLOW:
 *  1. User logs in → Keycloak validates credentials → returns JWT
 *  2. Backend decodes JWT → extracts email/firstName/lastName/required_actions
 *  3. If user not in local DB → 403 USER_NOT_REGISTERED (no auto-creation): an habilitation agent
 *     or central admin must first create the user (POST /admin/users) with the Keycloak username
 *  4. Registered user without role → signs in and sees the "waiting for a role" page
 *  5. Roles are assigned via /admin/users/{id}/roles; on next login / refresh the user gets access
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    @Value("${keycloak.token-url:https://10.2.60.146:9443/auth/realms/BCP-INTERNAL/protocol/openid-connect/token}")
    private String keycloakTokenUrl;

    @Value("${keycloak.client-id:Enrollement-Frontend}")
    private String keycloakClientId;

    @Value("${keycloak.client-secret:lzlnxrOnduPtdfiXP1TdQUDntJjqDIMs}")
    private String keycloakClientSecret;

    // ─────────────────────────── LOGIN ────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "Username and password are required", null));
        }

        try {
            // 1. Authenticate against Keycloak
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", keycloakClientId);
            params.add("client_secret", keycloakClientSecret);
            params.add("grant_type", "password");
            params.add("username", loginRequest.getUsername());
            params.add("password", loginRequest.getPassword());
            params.add("scope", "openid profile email");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            var kcResponse = restTemplate.postForEntity(
                keycloakTokenUrl,
                new HttpEntity<>(params, headers),
                Map.class
            );

            if (!kcResponse.getStatusCode().is2xxSuccessful() || kcResponse.getBody() == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("AUTH_FAILED", "Authentication failed", null));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenData = kcResponse.getBody();
            String accessToken  = (String) tokenData.get("access_token");
            String refreshToken = (String) tokenData.get("refresh_token");
            Long   expiresIn    = ((Number) tokenData.get("expires_in")).longValue();

            // 2. Decode JWT to extract user identity
            Map<String, Object> claims = decodeJwt(accessToken);
            String email     = (String) claims.getOrDefault("email", "");
            String firstName = (String) claims.getOrDefault("given_name", loginRequest.getUsername());
            String lastName  = (String) claims.getOrDefault("family_name", "");

            // 3. Only users registered beforehand by an habilitation agent / central admin may sign in.
            //    Keycloak's canonical username (preferred_username) is tried first, then the typed one.
            String tokenUsername = (String) claims.getOrDefault("preferred_username", "");
            User user = (tokenUsername.isBlank() ? Optional.<User>empty()
                    : userRepository.findByUsernameIncludingDeleted(tokenUsername))
                .or(() -> userRepository.findByUsernameIncludingDeleted(loginRequest.getUsername()))
                .orElse(null);
            if (user == null) {
                log.warn("Login refused - Keycloak user not registered in the application: {}", loginRequest.getUsername());
                auditService.log(loginRequest.getUsername(), "LOGIN_DENIED", "USER", null,
                        "Login refused: user " + loginRequest.getUsername() + " is not registered in the application");
                return ResponseEntity.status(403).body(
                    ApiResponse.error("USER_NOT_REGISTERED",
                        "Access denied: this user is not registered in the application", null));
            }
            if (user.isDeleted() || !Boolean.TRUE.equals(user.getIsActive())) {
                return ResponseEntity.status(403).body(
                    ApiResponse.error("ACCOUNT_DISABLED", "This account is disabled", null));
            }
            {
                // Keep DB in sync with Keycloak profile
                boolean changed = false;
                if (!email.isBlank() && !email.equals(user.getEmail())) { user.setEmail(email); changed = true; }
                if (!firstName.isBlank() && !firstName.equals(user.getFirstName())) { user.setFirstName(firstName); changed = true; }
                if (!lastName.isBlank() && !lastName.equals(user.getLastName())) { user.setLastName(lastName); changed = true; }
                if (changed) {
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                }
            }

            // 4. Load roles directly from userRoleRepository (avoids Hibernate L1 cache issue)
            List<String> roleNames = userRoleRepository.findActiveRolesByUsername(user.getUsername())
                .stream()
                .map(ur -> ur.getRole().getRoleName())
                .collect(Collectors.toList());

            LoginResponse loginResponse = LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleNames(roleNames)
                .build();

            log.info("Login successful - user: {}, roles: {}", user.getUsername(), roleNames);

            auditService.log(user.getUsername(), "LOGIN_SUCCESS", "USER", user.getId(),
                    "User " + user.getUsername() + " logged in successfully");

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));

        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            auditService.log(loginRequest.getUsername(), "LOGIN_FAILED", "USER", null,
                    "Failed login attempt for user: " + loginRequest.getUsername());
            return ResponseEntity.status(401).body(
                ApiResponse.error("INVALID_CREDENTIALS", "Invalid username or password", null));
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
            log.warn("Bad request during login for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(400).body(
                ApiResponse.error("BAD_REQUEST", "Login failed", null));
        } catch (Exception e) {
            log.error("Login error for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(500).body(
                ApiResponse.error("INTERNAL_ERROR", "An error occurred during login", null));
        }
    }

    // ─────────────────────────── ME ───────────────────────────────────────────

    /**
     * GET /auth/me - Returns current user's fresh data (including latest roles from DB).
     * Called on app startup to refresh stale localStorage state.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("UNAUTHORIZED", "Missing or invalid Authorization header", null));
        }
        String token = authHeader.substring(7);
        try {
            Map<String, Object> claims = decodeJwt(token);
            String username = (String) claims.getOrDefault("preferred_username", "");
            if (username.isBlank()) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("UNAUTHORIZED", "Cannot determine user from token", null));
            }

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return ResponseEntity.status(404).body(
                    ApiResponse.error("NOT_FOUND", "User not found in database", null));
            }
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                return ResponseEntity.status(403).body(
                    ApiResponse.error("ACCOUNT_DISABLED", "This account is disabled", null));
            }

            List<String> roleNames = userRoleRepository.findActiveRolesByUsername(username)
                .stream()
                .map(ur -> ur.getRole().getRoleName())
                .collect(Collectors.toList());

            LoginResponse me = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleNames(roleNames)
                .build();

            log.debug("GET /auth/me - user: {}, roles: {}", username, roleNames);
            return ResponseEntity.ok(ApiResponse.success(me));

        } catch (Exception e) {
            log.warn("GET /auth/me failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                ApiResponse.error("UNAUTHORIZED", "Invalid token", null));
        }
    }

    // ──────────────────────── REFRESH TOKEN ───────────────────────────────────

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "refreshToken is required", null));
        }

        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", keycloakClientId);
            params.add("client_secret", keycloakClientSecret);
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            var kcResponse = restTemplate.postForEntity(
                keycloakTokenUrl,
                new HttpEntity<>(params, headers),
                Map.class
            );

            if (!kcResponse.getStatusCode().is2xxSuccessful() || kcResponse.getBody() == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("REFRESH_FAILED", "Token refresh failed", null));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> tokenData = kcResponse.getBody();
            String newAccessToken  = (String) tokenData.get("access_token");
            String newRefreshToken = (String) tokenData.getOrDefault("refresh_token", refreshToken);
            Long   expiresIn       = ((Number) tokenData.get("expires_in")).longValue();

            Map<String, Object> refreshedClaims = decodeJwt(newAccessToken);
            String username = (String) refreshedClaims.getOrDefault("preferred_username", "");
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("ACCOUNT_DISABLED", "This account is disabled", null));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("token", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            result.put("expiresIn", expiresIn);

            log.info("Token refreshed successfully");
            // Decode new token to get username for audit
            try {
                Map<String, Object> claims = decodeJwt(newAccessToken);
                String uname = (String) claims.getOrDefault("preferred_username", "unknown");
                auditService.log(uname, "TOKEN_REFRESH", "USER", null, "Token refreshed for user: " + uname);
            } catch (Exception ignored) {}
            return ResponseEntity.ok(ApiResponse.success("Token refreshed", result));
        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                ApiResponse.error("REFRESH_FAILED", "Token refresh failed - please login again", null));
        }
    }

    // ──────────────────────── LOGOUT ──────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Map<String, Object> claims = decodeJwt(authHeader.substring(7));
                String username = (String) claims.getOrDefault("preferred_username", "unknown");
                auditService.log(username, "LOGOUT", "USER", null, "User " + username + " logged out");
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    // ──────────────────────── HELPERS ─────────────────────────────────────────

    /** Decode a JWT token payload (middle section) into a Map. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwt(String token) {
        try {
            String payload = token.split("\\.")[1];
            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            return objectMapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            log.warn("Could not decode JWT: {}", e.getMessage());
            return new HashMap<>();
        }
    }

}
