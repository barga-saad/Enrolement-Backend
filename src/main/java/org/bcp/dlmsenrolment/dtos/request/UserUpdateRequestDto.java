package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserUpdateRequestDto - Request DTO for updating an existing user.
 * 
 * Called by: PUT /admin/users/{id}
 * 
 * Note: Username and email cannot be changed (immutable identifiers)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDto {

    @Size(max = 100, message = "First name must be max 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be max 100 characters")
    private String lastName;

    private Boolean isActive;
}
