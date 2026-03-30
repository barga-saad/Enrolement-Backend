package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PermissionCreateRequestDto - Request DTO for creating a new permission.
 * 
 * Called by: POST /admin/permissions
 * 
 * Predefined permissions exist for each role and endpoint.
 * Admins can create custom permissions if needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionCreateRequestDto {

    @NotBlank(message = "Permission name is required")
    @Size(min = 3, max = 100, message = "Permission name must be 3-100 characters")
    private String permissionName;

    @NotBlank(message = "Endpoint is required")
    @Size(max = 255, message = "Endpoint must be max 255 characters")
    private String endpoint;

    @NotBlank(message = "HTTP method is required")
    @Size(max = 10, message = "HTTP method must be max 10 characters")
    private String httpMethod;

    @Size(max = 500, message = "Description must be max 500 characters")
    private String description;

    @Builder.Default
    private Boolean requiresOwnerCheck = false;

    @Builder.Default
    private Boolean isActive = true;
}
