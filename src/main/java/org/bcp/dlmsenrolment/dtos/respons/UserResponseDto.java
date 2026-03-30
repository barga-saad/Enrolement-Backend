package org.bcp.dlmsenrolment.dtos.respons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * UserResponseDto - Response DTO for User entity.
 * 
 * Returned by: GET /admin/users, GET /admin/users/{id}, POST/PUT/DELETE operations
 * 
 * Contains all user information including roles and audit data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    
    // Relationships
    private Set<String> roleNames; // Just the role names for simplicity
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Boolean isDeleted;
}
