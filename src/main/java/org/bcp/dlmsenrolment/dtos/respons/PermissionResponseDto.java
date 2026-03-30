package org.bcp.dlmsenrolment.dtos\respons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * PermissionResponseDto - Response DTO for Permission entity.
 * 
 * Returned by: GET /admin/permissions, GET /admin/permissions/{id}, POST/PUT/DELETE operations
 * 
 * Contains complete permission information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponseDto {

    private Long id;
    private String permissionName;
    private String endpoint;
    private String httpMethod;
    private String description;
    private Boolean requiresOwnerCheck;
    private Boolean isActive;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}
