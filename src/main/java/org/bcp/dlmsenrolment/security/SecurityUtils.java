package org.bcp.dlmsenrolment.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * SecurityUtils - centralizes principal name resolution.
 *
 * Problem: Keycloak JWTs have two user-identifying claims:
 *   - "sub"                → UUID like "2612bded-1411-4d79-96da-7e9c1dc256c4"  (default principal)
 *   - "preferred_username" → human-readable username like "john.doe"
 *
 * Spring's default JwtAuthenticationToken.getName() returns "sub" (the UUID).
 * This utility reads "preferred_username" directly from JWT claims.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the current authenticated user's preferred_username from the JWT.
     * Falls back to authentication.getName() (UUID) if claim is absent.
     * Falls back to "system" if no authentication exists.
     */
    public static String getCurrentUsername() {
        return getUsername(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Resolves preferred_username from a given Authentication object.
     * Use this in controllers where Authentication is already injected.
     */
    public static String getUsername(Authentication auth) {
        if (auth == null) return "system";

        // Spring OAuth2 Resource Server sets a JwtAuthenticationToken - read claim directly
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String preferred = jwtAuth.getToken().getClaimAsString("preferred_username");
            if (preferred != null && !preferred.isBlank()) {
                return preferred;
            }
        }

        String name = auth.getName();
        return (name != null && !name.isBlank()) ? name : "system";
    }
}
