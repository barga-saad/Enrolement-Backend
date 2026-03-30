package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AssignRoleToUserRequestDto - Request DTO for assigning a role to a user.
 * 
 * Called by: POST /admin/users/{userId}/roles
 * 
 * This creates a UserRole mapping, allowing the user to have a new role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRoleToUserRequestDto {

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
