package org.bcp.dlmsenrolment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.Permission;
import org.bcp.dlmsenrolment.entities.Role;
import org.bcp.dlmsenrolment.entities.RolePermission;
import org.bcp.dlmsenrolment.entities.User;
import org.bcp.dlmsenrolment.entities.UserRole;
import org.bcp.dlmsenrolment.repositories.PermissionRepository;
import org.bcp.dlmsenrolment.repositories.RolePermissionRepository;
import org.bcp.dlmsenrolment.repositories.RoleRepository;
import org.bcp.dlmsenrolment.repositories.UserRepository;
import org.bcp.dlmsenrolment.repositories.UserRoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DatabaseInitializer - Initialize seed data on application startup.
 * 
 * Creates 4 predefined roles with their associated permissions:
 * 1. PROFIL_SAISIE - Partner data entry permission
 * 2. PROFIL_VALIDATEUR - Partner validation permission
 * 3. PROFIL_CONSULTATION - Read-only consultation permission
 * 4. PROFIL_ADMIN_CENTRAL - Full admin access
 * 
 * Runs only ONCE on application startup (checks for existing roles).
 * Idempotent - safe to run multiple times.
 */
@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DatabaseInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Database Initializer Started ===");

        // Check if roles already exist (idempotency check)
        if (roleRepository.findByRoleName("PROFIL_SAISIE").isPresent()) {
            log.info("Seed data already exists. Skipping initialization.");
        } else {
            try {
                createPermissions();
                createRolesWithPermissions();
                createTestUsers();
                log.info("=== Database Initialization Completed Successfully ===");
            } catch (Exception e) {
                log.error("Error during database initialization", e);
                throw e;
            }
        }

        // Always ensure every user has the correct PROFIL_* role — runs on every startup
        ensureCorrectRoles();
    }

    /**
     * Ensures all known users have the correct PROFIL_* role assigned.
     * Safe to run multiple times (idempotent).
     *
     * Fixes the case where users were created with wrong role names
     * (ADMIN, VALIDATEUR, SAISIE, CONSULTATION) instead of the canonical
     * PROFIL_ names the frontend expects.
     */
    private void ensureCorrectRoles() {
        // username → correct PROFIL_ role name
        Map<String, String> assignments = new java.util.LinkedHashMap<>();
        assignments.put("admin",     "PROFIL_ADMIN_CENTRAL");
        assignments.put("sbarga",    "PROFIL_ADMIN_CENTRAL");
        assignments.put("john",      "PROFIL_VALIDATEUR");
        assignments.put("sarah",     "PROFIL_SAISIE");
        assignments.put("maria",     "PROFIL_CONSULTATION");

        for (Map.Entry<String, String> entry : assignments.entrySet()) {
            String username = entry.getKey();
            String roleName = entry.getValue();

            Role role = roleRepository.findByRoleName(roleName).orElse(null);
            if (role == null) {
                log.warn("ensureCorrectRoles: role {} not found, skipping {}", roleName, username);
                continue;
            }

            // Use eager-fetch query to avoid lazy-load issues
            User user = userRepository.findByUsernameWithRoles(username).orElse(null);
            if (user == null) {
                log.debug("ensureCorrectRoles: user {} not in DB yet (will be created on first login)", username);
                continue;
            }

            Optional<UserRole> existing = userRoleRepository.findByUserAndRole(user.getId(), role.getId());
            if (existing.isPresent()) {
                UserRole ur = existing.get();
                if (ur.getDeletedAt() != null) {
                    // Reactivate soft-deleted assignment
                    ur.setDeletedAt(null);
                    ur.setDeletedBy(null);
                    ur.setAssignedAt(LocalDateTime.now());
                    ur.setAssignedBy("system");
                    userRoleRepository.save(ur);
                    log.info("ensureCorrectRoles: reactivated {} for {}", roleName, username);
                } else {
                    log.debug("ensureCorrectRoles: {} already has {}", username, roleName);
                }
            } else {
                UserRole ur = new UserRole();
                ur.setUser(user);
                ur.setRole(role);
                ur.setAssignedAt(LocalDateTime.now());
                ur.setAssignedBy("system");
                userRoleRepository.save(ur);
                log.info("ensureCorrectRoles: assigned {} to {}", roleName, username);
            }
        }
    }

    /**
     * Create all permission records
     */
    private void createPermissions() {
        log.info("Creating permissions...");
        List<Permission> permissions = new ArrayList<>();

        // Partner Management Permissions
        permissions.add(Permission.builder()
            .permissionName("PARTNER_CREATE")
            .endpoint("/api/partners")
            .httpMethod("POST")
            .description("Create a new partner")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_LIST_ALL")
            .endpoint("/api/partners")
            .httpMethod("GET")
            .description("List all partners")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_VIEW_OWN")
            .endpoint("/api/partners/{id}")
            .httpMethod("GET")
            .description("View own partner record")
            .requiresOwnerCheck(true)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_UPDATE_OWN")
            .endpoint("/api/partners/{id}")
            .httpMethod("PUT")
            .description("Update own partner record")
            .requiresOwnerCheck(true)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_VALIDATE")
            .endpoint("/api/partners/{id}/validate")
            .httpMethod("POST")
            .description("Validate partner")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_DELETE")
            .endpoint("/api/partners/{id}")
            .httpMethod("DELETE")
            .description("Delete partner")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_SUBMIT")
            .endpoint("/v1/validations/partners/{id}/submit")
            .httpMethod("POST")
            .description("Submit partner for validation")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_REJECT")
            .endpoint("/v1/validations/{id}/reject")
            .httpMethod("POST")
            .description("Reject partner validation")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_VALIDATION_LIST")
            .endpoint("/v1/validations/pending")
            .httpMethod("GET")
            .description("List pending validations")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("AUDIT_LOG_VIEW")
            .endpoint("/admin/audit-logs")
            .httpMethod("GET")
            .description("View audit logs")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_EXPORT")
            .endpoint("/v1/export/partners")
            .httpMethod("GET")
            .description("Export partners to CSV")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // Transfer Permissions
        permissions.add(Permission.builder()
            .permissionName("TRANSFER_CREATE")
            .endpoint("/api/transfers")
            .httpMethod("POST")
            .description("Create a new transfer")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("TRANSFER_LIST_ALL")
            .endpoint("/api/transfers")
            .httpMethod("GET")
            .description("List all transfers")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("TRANSFER_VIEW_OWN")
            .endpoint("/api/transfers/{id}")
            .httpMethod("GET")
            .description("View own transfer record")
            .requiresOwnerCheck(true)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("TRANSFER_VALIDATE")
            .endpoint("/api/transfers/{id}/validate")
            .httpMethod("POST")
            .description("Validate transfer")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // Report & Export Permissions
        permissions.add(Permission.builder()
            .permissionName("REPORT_EXPORT")
            .endpoint("/api/reports/export")
            .httpMethod("POST")
            .description("Export reports")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("DOCUMENT_PRINT")
            .endpoint("/api/documents/print")
            .httpMethod("POST")
            .description("Print documents")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // Admin Management Permissions
        permissions.add(Permission.builder()
            .permissionName("ADMIN_USER_MANAGE")
            .endpoint("/admin/users")
            .httpMethod("POST")
            .description("Create and manage users")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_ROLE_MANAGE")
            .endpoint("/admin/roles")
            .httpMethod("POST")
            .description("Create and manage roles")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_PERMISSION_MANAGE")
            .endpoint("/admin/permissions")
            .httpMethod("POST")
            .description("Create and manage permissions")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissionRepository.saveAll(permissions);
        log.info("Created {} permissions", permissions.size());
    }

    /**
     * Create 4 predefined roles and assign permissions
     */
    private void createRolesWithPermissions() {
        log.info("Creating roles and assigning permissions...");

        // Role 1: PROFIL_SAISIE - Partner Data Entry (Can only work with their own records)
        Role saisieRole = Role.builder()
            .roleName("PROFIL_SAISIE")
            .description("Partner Data Entry - Can create and update own partner records")
            .isActive(true)
            .build();
        roleRepository.save(saisieRole);

        assignPermissionsToRole(saisieRole, "PARTNER_CREATE", "PARTNER_LIST_ALL", 
            "PARTNER_VIEW_OWN", "PARTNER_UPDATE_OWN", "TRANSFER_CREATE", "TRANSFER_VIEW_OWN",
            "PARTNER_SUBMIT");
        log.info("Created role: PROFIL_SAISIE");

        // Role 2: PROFIL_VALIDATEUR - Validator (Can validate all partner/transfer records)
        Role validateurRole = Role.builder()
            .roleName("PROFIL_VALIDATEUR")
            .description("Validator - Can view and validate partner and transfer records")
            .isActive(true)
            .build();
        roleRepository.save(validateurRole);

        assignPermissionsToRole(validateurRole, "PARTNER_LIST_ALL", "PARTNER_VALIDATE",
            "TRANSFER_LIST_ALL", "TRANSFER_VALIDATE", "REPORT_EXPORT",
            "PARTNER_REJECT", "PARTNER_VALIDATION_LIST", "PARTNER_EXPORT");
        log.info("Created role: PROFIL_VALIDATEUR");

        // Role 3: PROFIL_CONSULTATION - Consultation (Read-only access)
        Role consultationRole = Role.builder()
            .roleName("PROFIL_CONSULTATION")
            .description("Consultation - Read-only access to partner and transfer records")
            .isActive(true)
            .build();
        roleRepository.save(consultationRole);

        assignPermissionsToRole(consultationRole, "PARTNER_LIST_ALL", "TRANSFER_LIST_ALL", 
            "DOCUMENT_PRINT");
        log.info("Created role: PROFIL_CONSULTATION");

        // Role 4: PROFIL_ADMIN_CENTRAL - Full Admin (All permissions)
        Role adminRole = Role.builder()
            .roleName("PROFIL_ADMIN_CENTRAL")
            .description("Central Admin - Full system access including user and role management")
            .isActive(true)
            .build();
        roleRepository.save(adminRole);

        // Assign ALL permissions to admin role
        List<Permission> allPermissions = permissionRepository.findAllActive();
        for (Permission permission : allPermissions) {
            RolePermission rolePermission = RolePermission.builder()
                .role(adminRole)
                .permission(permission)
                .build();
            rolePermissionRepository.save(rolePermission);
        }
        log.info("Created role: PROFIL_ADMIN_CENTRAL with {} permissions", allPermissions.size());
    }

    /**
     * Helper method to assign specific permissions to a role
     */
    private void assignPermissionsToRole(Role role, String... permissionNames) {
        List<Permission> permissions = new ArrayList<>();
        
        for (String permissionName : permissionNames) {
            permissionRepository.findByPermissionName(permissionName)
                .ifPresent(permissions::add);
        }

        for (Permission permission : permissions) {
            RolePermission rolePermission = RolePermission.builder()
                .role(role)
                .permission(permission)
                .build();
            rolePermissionRepository.save(rolePermission);
        }
    }

    /**
     * Create test users with their respective roles
     * Test Users:
     * 1. Admin: admin with PROFIL_ADMIN_CENTRAL role
     * 2. Data Entry: sarah with PROFIL_SAISIE role
     * 3. Validator: validator with PROFIL_VALIDATEUR role
     */
    private void createTestUsers() {
        log.info("Creating test users...");

        // Get roles
        Role adminRole = roleRepository.findByRoleName("PROFIL_ADMIN_CENTRAL").orElse(null);
        Role saisieRole = roleRepository.findByRoleName("PROFIL_SAISIE").orElse(null);
        Role validateurRole = roleRepository.findByRoleName("PROFIL_VALIDATEUR").orElse(null);

        if (adminRole == null || saisieRole == null || validateurRole == null) {
            log.warn("Could not create test users: Roles not found");
            return;
        }

        // Test User 1: Admin
        if (!userRepository.findByUsername("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@enrollment.local");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("Central");
            adminUser.setIsActive(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());

            User savedAdmin = userRepository.save(adminUser);

            UserRole adminUserRole = new UserRole();
            adminUserRole.setUser(savedAdmin);
            adminUserRole.setRole(adminRole);
            adminUserRole.setAssignedAt(LocalDateTime.now());
            adminUserRole.setAssignedBy("system");
            userRoleRepository.save(adminUserRole);

            log.info("Created test user: admin with PROFIL_ADMIN_CENTRAL role");
        }

        // Test User 2: Data Entry (Sarah)
        if (!userRepository.findByUsername("sarah").isPresent()) {
            User sarahUser = new User();
            sarahUser.setUsername("sarah");
            sarahUser.setEmail("sarah@enrollment.local");
            sarahUser.setFirstName("Sarah");
            sarahUser.setLastName("DataEntry");
            sarahUser.setIsActive(true);
            sarahUser.setCreatedAt(LocalDateTime.now());
            sarahUser.setUpdatedAt(LocalDateTime.now());

            User savedSarah = userRepository.save(sarahUser);

            UserRole sarahUserRole = new UserRole();
            sarahUserRole.setUser(savedSarah);
            sarahUserRole.setRole(saisieRole);
            sarahUserRole.setAssignedAt(LocalDateTime.now());
            sarahUserRole.setAssignedBy("system");
            userRoleRepository.save(sarahUserRole);

            log.info("Created test user: sarah with PROFIL_SAISIE role");
        }

        // Test User 3: Validator
        if (!userRepository.findByUsername("validator").isPresent()) {
            User validatorUser = new User();
            validatorUser.setUsername("validator");
            validatorUser.setEmail("validator@enrollment.local");
            validatorUser.setFirstName("Jean");
            validatorUser.setLastName("Validateur");
            validatorUser.setIsActive(true);
            validatorUser.setCreatedAt(LocalDateTime.now());
            validatorUser.setUpdatedAt(LocalDateTime.now());

            User savedValidator = userRepository.save(validatorUser);

            UserRole validatorUserRole = new UserRole();
            validatorUserRole.setUser(savedValidator);
            validatorUserRole.setRole(validateurRole);
            validatorUserRole.setAssignedAt(LocalDateTime.now());
            validatorUserRole.setAssignedBy("system");
            userRoleRepository.save(validatorUserRole);

            log.info("Created test user: validator with PROFIL_VALIDATEUR role");
        }

        log.info("Test users setup completed");
    }
}
