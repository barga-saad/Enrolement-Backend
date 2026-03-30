package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoleUpdateRequestDto - Request DTO for updating an existing role.
 * 
 * Called by: PUT /admin/roles/{id}
 * 
 * Note: Role name cannot be changed (immutable identifier)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequestDto {

    @Size(max = 500, message = "Description must be max 500 characters")
    private String description;

    private Boolean isActive;
}
