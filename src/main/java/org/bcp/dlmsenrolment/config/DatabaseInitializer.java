package org.bcp.dlmsenrolment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.Permission;
import org.bcp.dlmsenrolment.entities.Role;
import org.bcp.dlmsenrolment.entities.RolePermission;
import org.bcp.dlmsenrolment.repositories.PermissionRepository;
import org.bcp.dlmsenrolment.repositories.RolePermissionRepository;
import org.bcp.dlmsenrolment.repositories.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Database Initializer Started ===");

        // Check if roles already exist (idempotency check)
        if (roleRepository.findByRoleName("PROFIL_SAISIE").isPresent()) {
            log.info("Seed data already exists. Skipping initialization.");
            return;
        }

        try {
            // Create all permissions first
            createPermissions();

            // Create roles and assign permissions
            createRolesWithPermissions();

            log.info("=== Database Initialization Completed Successfully ===");
        } catch (Exception e) {
            log.error("Error during database initialization", e);
            throw e;
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
            "PARTNER_VIEW_OWN", "PARTNER_UPDATE_OWN", "TRANSFER_CREATE", "TRANSFER_VIEW_OWN");
        log.info("Created role: PROFIL_SAISIE");

        // Role 2: PROFIL_VALIDATEUR - Validator (Can validate all partner/transfer records)
        Role validateurRole = Role.builder()
            .roleName("PROFIL_VALIDATEUR")
            .description("Validator - Can view and validate partner and transfer records")
            .isActive(true)
            .build();
        roleRepository.save(validateurRole);

        assignPermissionsToRole(validateurRole, "PARTNER_LIST_ALL", "PARTNER_VALIDATE",
            "TRANSFER_LIST_ALL", "TRANSFER_VALIDATE", "REPORT_EXPORT");
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
}
