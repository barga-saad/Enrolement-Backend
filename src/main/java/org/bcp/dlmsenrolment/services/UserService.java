package org.bcp.dlmsenrolment.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.dtos.request.AssignRoleToUserRequestDto;
import org.bcp.dlmsenrolment.dtos.request.UserCreateRequestDto;
import org.bcp.dlmsenrolment.dtos.request.UserUpdateRequestDto;
import org.bcp.dlmsenrolment.dtos.respons.UserResponseDto;
import org.bcp.dlmsenrolment.entities.Role;
import org.bcp.dlmsenrolment.entities.User;
import org.bcp.dlmsenrolment.entities.UserRole;
import org.bcp.dlmsenrolment.repositories.RoleRepository;
import org.bcp.dlmsenrolment.repositories.UserRepository;
import org.bcp.dlmsenrolment.repositories.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserService - Business logic for user management.
 * 
 * CRUD operations:
 * - CREATE: Create new user (by admin)
 * - READ: Get user by ID or username
 * - UPDATE: Update user details
 * - DELETE: Soft-delete user
 * 
 * Role management:
 * - ASSIGN: Assign role to user
 * - REVOKE: Revoke role from user
 * - LIST: Get user's roles
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Create a new user
     * 
     * @param dto User creation request
     * @param createdByUsername Admin username
     * @return Created user as response DTO
     */
    public UserResponseDto createUser(UserCreateRequestDto dto, String createdByUsername) {
        log.info("Creating new user: {}", dto.getUsername());

        // Check if user already exists
        if (userRepository.existsByUsernameActive(dto.getUsername())) {
            throw new RuntimeException("User with username '" + dto.getUsername() + "' already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User with email '" + dto.getEmail() + "' already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // Save
        User saved = userRepository.save(user);
        log.info("User created successfully: {}", saved.getId());

        return mapToResponseDto(saved);
    }

    /**
     * Get user by ID
     */
    public UserResponseDto getUserById(Long id) {
        return userRepository.findById(id)
            .filter(u -> !u.isDeleted())
            .map(this::mapToResponseDto)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    /**
     * Get user by username
     */
    public UserResponseDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(this::mapToResponseDto)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Get all active users
     */
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAllActive()
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Update user details
     */
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (user.isDeleted()) {
            throw new RuntimeException("Cannot update deleted user: " + id);
        }

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        User updated = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return mapToResponseDto(updated);
    }

    /**
     * Soft-delete user
     */
    public void deleteUser(Long id, String deletedByUsername) {
        log.warn("Deleting user: {} by {}", id, deletedByUsername);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (user.isDeleted()) {
            throw new RuntimeException("User already deleted: " + id);
        }

        // Soft-delete user
        user.softDelete(deletedByUsername);

        // Soft-delete all user's roles (userRoles cascade)
        userRoleRepository.findActiveRolesByUserId(id)
            .forEach(ur -> ur.revoke(deletedByUsername));

        userRepository.save(user);
        log.info("User soft-deleted successfully: {}", id);
    }

    /**
     * Restore a soft-deleted user
     */
    public void restoreUser(Long id) {
        log.info("Restoring user: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        if (!user.isDeleted()) {
            throw new RuntimeException("User is not deleted: " + id);
        }

        user.restore();
        userRepository.save(user);
        log.info("User restored successfully: {}", id);
    }

    /**
     * Assign a role to a user
     */
    public void assignRoleToUser(Long userId, Long roleId, String assignedByUsername) {
        log.info("Assigning role {} to user {}", roleId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        if (user.isDeleted()) {
            throw new RuntimeException("Cannot assign role to deleted user");
        }

        if (role.isDeleted()) {
            throw new RuntimeException("Cannot assign deleted role");
        }

        // Check if already assigned
        if (userRoleRepository.userHasRole(userId, roleId)) {
            throw new RuntimeException("User already has this role");
        }

        // Create new user-role mapping
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedByUsername);

        userRoleRepository.save(userRole);
        log.info("Role assigned successfully: user {} <- role {}", userId, roleId);
    }

    /**
     * Revoke a role from a user
     */
    public void revokeRoleFromUser(Long userId, Long roleId, String revokedByUsername) {
        log.info("Revoking role {} from user {}", roleId, userId);

        UserRole userRole = userRoleRepository.findByUserAndRole(userId, roleId)
            .orElseThrow(() -> new RuntimeException("User-role assignment not found"));

        if (!userRole.isActive()) {
            throw new RuntimeException("Role is already revoked for this user");
        }

        userRole.revoke(revokedByUsername);
        userRoleRepository.save(userRole);
        log.info("Role revoked successfully: user {} -> role {}", userId, roleId);
    }

    /**
     * Get all roles for a user
     */
    public List<String> getUserRoles(Long userId) {
        return userRoleRepository.findActiveRolesByUserId(userId)
            .stream()
            .map(ur -> ur.getRole().getRoleName())
            .collect(Collectors.toList());
    }

    /**
     * Map User entity to response DTO
     */
    private UserResponseDto mapToResponseDto(User user) {
        List<String> roleNames = userRoleRepository.findActiveRolesByUserId(user.getId())
            .stream()
            .map(ur -> ur.getRole().getRoleName())
            .collect(Collectors.toList());

        return UserResponseDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .isActive(user.getIsActive())
            .roleNames(roleNames.stream().collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .deletedAt(user.getDeletedAt())
            .isDeleted(user.isDeleted())
            .build();
    }
}
