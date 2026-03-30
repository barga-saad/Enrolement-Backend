package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AssignPermissionToRoleRequestDto - Request DTO for assigning a permission to a role.
 * 
 * Called by: POST /admin/roles/{roleId}/permissions
 * 
 * This creates a RolePermission mapping, giving the role access to an endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignPermissionToRoleRequestDto {

    @NotNull(message = "Permission ID is required")
    private Long permissionId;
}
