package org.bcp.dlmsenrolment.dtos.respons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * RoleResponseDto - Response DTO for Role entity.
 * 
 * Returned by: GET /admin/roles, GET /admin/roles/{id}, POST/PUT/DELETE operations
 * 
 * Contains role information with associated permissions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseDto {

    private Long id;
    private String roleName;
    private String description;
    private Boolean isActive;
    
    // Relationships
    private Set<String> permissionNames; // Permission names for this role
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}
