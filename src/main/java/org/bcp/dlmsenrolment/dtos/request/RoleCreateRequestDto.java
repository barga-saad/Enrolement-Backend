package org.bcp.dlmsenrolment.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RoleCreateRequestDto - Request DTO for creating a new role.
 * 
 * Called by: POST /admin/roles
 * 
 * Predefined roles (cannot be created):
 * - PROFIL_SAISIE
 * - PROFIL_VALIDATEUR
 * - PROFIL_CONSULTATION
 * - PROFIL_ADMIN_CENTRAL
 * 
 * But admin can create custom roles if needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreateRequestDto {

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 100, message = "Role name must be 3-100 characters")
    private String roleName;

    @Size(max = 500, message = "Description must be max 500 characters")
    private String description;

    @Builder.Default
    private Boolean isActive = true;
}
