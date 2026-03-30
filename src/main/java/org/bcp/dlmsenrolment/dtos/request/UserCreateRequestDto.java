package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserCreateRequestDto - Request DTO for creating a new user.
 * 
 * Called by: POST /admin/users
 * 
 * Validation:
 * - username: Required, unique, 3-50 chars
 * - email: Required, unique, valid email format
 * - firstName, lastName: Optional, max 100 chars
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be 3-100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100, message = "First name must be max 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be max 100 characters")
    private String lastName;

    @Builder.Default
    private Boolean isActive = true;
}
