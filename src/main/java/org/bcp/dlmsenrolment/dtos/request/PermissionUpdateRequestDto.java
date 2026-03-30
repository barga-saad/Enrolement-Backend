package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PermissionUpdateRequestDto - Request DTO for updating an existing permission.
 * 
 * Called by: PUT /admin/permissions/{id}
 * 
 * Note: permission name, endpoint, and method cannot be changed (immutable)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionUpdateRequestDto {

    @Size(max = 500, message = "Description must be max 500 characters")
    private String description;

    private Boolean requiresOwnerCheck;

    private Boolean isActive;
}
