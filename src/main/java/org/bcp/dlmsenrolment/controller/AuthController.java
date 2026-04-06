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
 * AuthController — Handles Keycloak-based login, auto user sync, and password change.
 *
 * FLOW:
 *  1. User logs in → Keycloak validates credentials → returns JWT
 *  2. Backend decodes JWT → extracts email/firstName/lastName/required_actions
 *  3. If user not in local DB → auto-create (no roles yet)
 *  4. If JWT has required_actions=[UPDATE_PASSWORD] → return requiresPasswordChange=true
 *  5. Admin assigns roles via /admin/users/{id}/roles
 *  6. On next login user gets proper roleNames and correct access
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

    @Value("${keycloak.token-url:http://localhost:8081/realms/master/protocol/openid-connect/token}")
    private String keycloakTokenUrl;

    @Value("${keycloak.client-id:Enrollement-Frontend}")
    private String keycloakClientId;

    @Value("${keycloak.client-secret:lzlnxrOnduPtdfiXP1TdQUDntJjqDIMs}")
    private String keycloakClientSecret;

    @Value("${keycloak.admin-url:http://localhost:8081/admin/realms/master}")
    private String keycloakAdminUrl;

    @Value("${keycloak.admin-username:admin}")
    private String keycloakAdminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String keycloakAdminPassword;

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

            // 2. Decode JWT to extract user identity and required actions
            Map<String, Object> claims = decodeJwt(accessToken);
            String email     = (String) claims.getOrDefault("email", "");
            String firstName = (String) claims.getOrDefault("given_name", loginRequest.getUsername());
            String lastName  = (String) claims.getOrDefault("family_name", "");

            @SuppressWarnings("unchecked")
            List<String> requiredActions = (List<String>) claims.getOrDefault("required_actions", List.of());
            boolean requiresPasswordChange = requiredActions.contains("UPDATE_PASSWORD");

            // 3. Auto-sync: create user in local DB if not yet registered
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(loginRequest.getUsername());
                user.setEmail(email.isBlank() ? loginRequest.getUsername() + "@enrollment.local" : email);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setIsActive(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Auto-synced new user from Keycloak: {}", loginRequest.getUsername());
            } else {
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
            List<String> roleNames = userRoleRepository.findActiveRolesByUsername(loginRequest.getUsername())
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
                .requiresPasswordChange(requiresPasswordChange)
                .build();

            log.info("Login successful - user: {}, roles: {}, requiresPasswordChange: {}",
                user.getUsername(), roleNames, requiresPasswordChange);

            return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));

        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body(
                ApiResponse.error("INVALID_CREDENTIALS", "Invalid username or password", null));
        } catch (org.springframework.web.client.HttpClientErrorException.BadRequest e) {
            String body = e.getResponseBodyAsString();
            if (body.contains("Account is not fully set up")) {
                log.info("User {} has temporary password — requires password change", loginRequest.getUsername());
                return handleTemporaryPasswordUser(loginRequest);
            }
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
                .requiresPasswordChange(false)
                .build();

            log.debug("GET /auth/me — user: {}, roles: {}", username, roleNames);
            return ResponseEntity.ok(ApiResponse.success(me));

        } catch (Exception e) {
            log.warn("GET /auth/me failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                ApiResponse.error("UNAUTHORIZED", "Invalid token", null));
        }
    }

    // ──────────────────────── CHANGE PASSWORD ─────────────────────────────────

    /**
     * POST /auth/change-password
     * Allows a user to change their Keycloak password (e.g. after first login with temp password).
     * Uses the Keycloak Admin REST API to set the new password and clear required actions.
     *
     * Body: { "username": "sarah", "newPassword": "NewPass123!" }
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String username    = request.get("username");
        String newPassword = request.get("newPassword");

        if (username == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "Username and newPassword (min 6 chars) are required", null));
        }

        try {
            // 1. Get Keycloak admin token
            String adminToken = getAdminToken();

            // 2. Find the Keycloak user ID by username
            String keycloakUserId = findKeycloakUserId(username, adminToken);
            if (keycloakUserId == null) {
                return ResponseEntity.status(404).body(
                    ApiResponse.error("USER_NOT_FOUND", "User not found in Keycloak: " + username, null));
            }

            // 3. Reset password in Keycloak (temporary = false → permanent)
            resetKeycloakPassword(keycloakUserId, newPassword, adminToken);

            // 4. Clear required actions (removes UPDATE_PASSWORD)
            clearRequiredActions(keycloakUserId, adminToken);

            log.info("Password changed successfully for user: {}", username);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));

        } catch (Exception e) {
            log.error("Password change failed for user: {}", username, e);
            return ResponseEntity.status(500).body(
                ApiResponse.error("PASSWORD_CHANGE_FAILED", "Failed to change password: " + e.getMessage(), null));
        }
    }

    // ──────────────────────── LOGOUT ──────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    // ──────────────────────── HELPERS ─────────────────────────────────────────

    /**
     * Handle login for users with temporary passwords.
     * Keycloak won't issue a token, so we use the Admin API to get user info,
     * auto-sync to local DB, and return requiresPasswordChange=true.
     */
    private ResponseEntity<?> handleTemporaryPasswordUser(LoginRequest loginRequest) {
        try {
            String adminToken = getAdminToken();
            String keycloakUserId = findKeycloakUserId(loginRequest.getUsername(), adminToken);
            if (keycloakUserId == null) {
                return ResponseEntity.status(404).body(
                    ApiResponse.error("USER_NOT_FOUND", "User not found in Keycloak", null));
            }

            // Get user details from Keycloak Admin API
            @SuppressWarnings("unchecked")
            Map<String, Object> kcUser = getKeycloakUser(keycloakUserId, adminToken);
            String email     = (String) kcUser.getOrDefault("email", "");
            String firstName = (String) kcUser.getOrDefault("firstName", loginRequest.getUsername());
            String lastName  = (String) kcUser.getOrDefault("lastName", "");

            // Auto-sync to local DB
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(loginRequest.getUsername());
                user.setEmail(email != null && !email.isBlank() ? email : loginRequest.getUsername() + "@enrollment.local");
                user.setFirstName(firstName != null ? firstName : "");
                user.setLastName(lastName != null ? lastName : "");
                user.setIsActive(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Auto-synced temp-password user from Keycloak: {}", loginRequest.getUsername());
            }

            List<String> roleNames = userRoleRepository.findActiveRolesByUsername(loginRequest.getUsername())
                .stream()
                .map(ur -> ur.getRole().getRoleName())
                .collect(Collectors.toList());

            LoginResponse loginResponse = LoginResponse.builder()
                .token(null)
                .requiresPasswordChange(true)
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleNames(roleNames)
                .build();

            return ResponseEntity.ok(ApiResponse.success("Password change required", loginResponse));
        } catch (Exception ex) {
            log.error("Error handling temp password user: {}", loginRequest.getUsername(), ex);
            return ResponseEntity.status(500).body(
                ApiResponse.error("INTERNAL_ERROR", "Failed to process temporary password user", null));
        }
    }

    /** Get user details from Keycloak Admin API by user ID. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getKeycloakUser(String userId, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String url = keycloakAdminUrl + "/users/" + userId;
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return response.getBody() != null ? response.getBody() : new HashMap<>();
    }

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

    /** Get a Keycloak admin access token using the admin credentials. */
    private String getAdminToken() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", "admin-cli");
        params.add("username", keycloakAdminUsername);
        params.add("password", keycloakAdminPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Admin token URL uses the master realm
        String adminTokenUrl = keycloakAdminUrl.replace("/admin/realms/master", "")
            + "/realms/master/protocol/openid-connect/token";

        @SuppressWarnings("unchecked")
        Map<String, Object> body = restTemplate.postForObject(
            adminTokenUrl,
            new HttpEntity<>(params, headers),
            Map.class
        );

        if (body == null || body.get("access_token") == null) {
            throw new RuntimeException("Could not obtain Keycloak admin token");
        }
        return (String) body.get("access_token");
    }

    /** Find a user's Keycloak internal ID by username using the Admin API. */
    private String findKeycloakUserId(String username, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String url = keycloakAdminUrl + "/users?username=" + username + "&exact=true";
        var response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> users = response.getBody();
        if (users == null || users.isEmpty()) return null;

        return (String) users.get(0).get("id");
    }

    /** Set a new permanent password in Keycloak for the given user ID. */
    private void resetKeycloakPassword(String userId, String newPassword, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("type", "password");
        body.put("value", newPassword);
        body.put("temporary", false);

        String url = keycloakAdminUrl + "/users/" + userId + "/reset-password";
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
        log.debug("Password reset in Keycloak for user ID: {}", userId);
    }

    /** Clear all required actions (e.g. UPDATE_PASSWORD) for the given user. */
    private void clearRequiredActions(String userId, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("requiredActions", List.of());

        String url = keycloakAdminUrl + "/users/" + userId;
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
        log.debug("Cleared required actions for user ID: {}", userId);
    }
}
