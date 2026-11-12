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
import org.bcp.dlmsenrolment.excptions.ForbiddenOperationException;
import org.bcp.dlmsenrolment.security.Profiles;
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
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("User with username '" + dto.getUsername() + "' already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmailIncludingDeleted(dto.getEmail()).isPresent()) {
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
        return userRepository.findAllForAdmin()
            .stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    /**
     * Update user details
     */
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto, String updatedByUsername) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
        ensureCanManageUser(updatedByUsername, user);

        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getIsActive() != null) {
            if (dto.getIsActive()) {
                user.activate();
            } else {
                deactivateUser(user, updatedByUsername);
            }
        }

        User updated = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return mapToResponseDto(updated);
    }

    /**
     * Soft-delete user
     */
    public void deleteUser(Long id, String deletedByUsername) {
        log.info("Deactivating user: {} by {}", id, deletedByUsername);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
        ensureCanManageUser(deletedByUsername, user);

        if (!Boolean.TRUE.equals(user.getIsActive()) && !user.isDeleted()) {
            throw new RuntimeException("User already inactive: " + id);
        }

        deactivateUser(user, deletedByUsername);

        userRepository.save(user);
        log.info("User deactivated successfully: {}", id);
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

        user.activate();
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
        ensureCanManageUser(assignedByUsername, user);
        ensureCanGrantRole(assignedByUsername, role);

        if (user.isDeleted() || !Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Cannot assign role to inactive user");
        }

        if (role.isDeleted() || !Boolean.TRUE.equals(role.getIsActive())) {
            throw new RuntimeException("Cannot assign inactive role");
        }

        Optional<UserRole> existingAssignment = userRoleRepository.findByUserAndRole(userId, roleId);
        if (existingAssignment.isPresent()) {
            UserRole assignment = existingAssignment.get();
            if (assignment.isActive()) {
                throw new RuntimeException("User already has this role");
            }
            assignment.restore();
            assignment.setAssignedBy(assignedByUsername);
            userRoleRepository.save(assignment);
            log.info("Role assignment restored: user {} <- role {}", userId, roleId);
            return;
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
        ensureCanManageUser(revokedByUsername, userRole.getUser());
        ensureCanGrantRole(revokedByUsername, userRole.getRole());

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

    // ===== Profile restrictions (agent d'habilitation) =====

    private boolean isCentralAdmin(String username) {
        return username != null && activeRoleNames(username).contains(Profiles.ADMIN_CENTRAL);
    }

    private java.util.Set<String> activeRoleNames(String username) {
        return userRoleRepository.findActiveRolesByUsername(username).stream()
            .map(ur -> ur.getRole().getRoleName())
            .collect(Collectors.toSet());
    }

    /** Only a central admin may grant or revoke roles other than agent d'enrôlement / consultant. */
    private void ensureCanGrantRole(String actorUsername, Role role) {
        if (isCentralAdmin(actorUsername)) {
            return;
        }
        if (!Profiles.ASSIGNABLE_BY_HABILITATION.contains(role.getRoleName())) {
            throw new ForbiddenOperationException(
                "Only a central administrator can assign or revoke the role " + role.getRoleName());
        }
    }

    /** Only a central admin may modify an account holding PROFIL_ADMIN_CENTRAL or PROFIL_AGENT_HABILITATION. */
    private void ensureCanManageUser(String actorUsername, User target) {
        if (isCentralAdmin(actorUsername)) {
            return;
        }
        boolean protectedTarget = userRoleRepository.findActiveRolesByUserId(target.getId()).stream()
            .anyMatch(ur -> Profiles.PROTECTED_FROM_HABILITATION.contains(ur.getRole().getRoleName()));
        if (protectedTarget) {
            throw new ForbiddenOperationException(
                "Only a central administrator can modify an administrator or habilitation agent account");
        }
    }

    private void deactivateUser(User user, String deactivatedByUsername) {
        user.deactivate();
        userRoleRepository.findActiveRolesByUserId(user.getId())
            .forEach(userRole -> userRole.revoke(deactivatedByUsername));
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
