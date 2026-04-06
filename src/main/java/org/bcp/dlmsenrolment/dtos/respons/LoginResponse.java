package org.bcp.dlmsenrolment.dtos.respons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Login Response DTO
 * JWT token response after successful authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roleNames;
    private boolean requiresPasswordChange;
}
