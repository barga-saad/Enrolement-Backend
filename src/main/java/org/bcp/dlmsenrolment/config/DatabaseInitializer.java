package org.bcp.dlmsenrolment.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bcp.dlmsenrolment.entities.Permission;
import org.bcp.dlmsenrolment.entities.Role;
import org.bcp.dlmsenrolment.entities.RolePermission;
import org.bcp.dlmsenrolment.entities.UserRole;
import org.bcp.dlmsenrolment.security.Profiles;
import org.bcp.dlmsenrolment.repositories.PermissionRepository;
import org.bcp.dlmsenrolment.repositories.RolePermissionRepository;
import org.bcp.dlmsenrolment.repositories.RoleRepository;
import org.bcp.dlmsenrolment.repositories.UserRepository;
import org.bcp.dlmsenrolment.repositories.UserRoleRepository;
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
                log.info("=== Database Initialization Completed Successfully ===");
            } catch (Exception e) {
                log.error("Error during database initialization", e);
                throw e;
            }
        }

        reconcileAdminPermissions();
        reconcileAdminPermissionUpdate();
        reconcileAdminPartnerStatsPermission();
        reconcilePartnerExportPermission();
        reconcilePartnerStatusPermission();
        reconcileRemovedTransferPermissions();
        reconcileAgentEnrolementProfile();
        reconcileAgentHabilitationProfile();
    }

    // ===== PROFILES: agent d'enrôlement + agent d'habilitation =====

    /** Data entry + validation (4-eyes rule enforced in PartnerValidationServiceImpl). */
    private static final List<String> AGENT_ENROLEMENT_PERMISSIONS = List.of(
        "PARTNER_CREATE", "PARTNER_LIST_ALL", "PARTNER_VIEW_OWN", "PARTNER_UPDATE_OWN", "PARTNER_STATUS_CHANGE",
        "PARTNER_SUBMIT", "PARTNER_DRAFT", "PARTNER_DRAFTS_LIST", "PARTNER_VALIDATION_VIEW",
        "PARTNER_VALIDATE", "PARTNER_REJECT", "PARTNER_VALIDATION_LIST", "PARTNER_EXPORT");

    /** User management and role assignment only (restrictions enforced in UserService). */
    private static final List<String> AGENT_HABILITATION_PERMISSIONS = List.of(
        "ADMIN_USER_VIEW", "ADMIN_USER_VIEW_ONE", "ADMIN_USER_MANAGE", "ADMIN_USER_UPDATE", "ADMIN_USER_DELETE",
        "ADMIN_ROLE_ASSIGN", "ADMIN_ROLE_REVOKE", "ADMIN_ROLE_VIEW");

    /**
     * Merges PROFIL_SAISIE and PROFIL_VALIDATEUR into PROFIL_AGENT_ENROLEMENT:
     * creates the new profile, moves every user holding an old profile to it, then revokes the old
     * assignments and deactivates the old profiles (kept in DB for history and audit).
     */
    private void reconcileAgentEnrolementProfile() {
        Role agentRole = ensureRole(Profiles.AGENT_ENROLEMENT,
            "Agent d'enrôlement - saisie et validation des partenaires/clients (principe des 4 yeux)");

        List<String> permissionNames = new ArrayList<>(AGENT_ENROLEMENT_PERMISSIONS);
        for (String legacyName : List.of(Profiles.LEGACY_SAISIE, Profiles.LEGACY_VALIDATEUR)) {
            roleRepository.findByRoleName(legacyName).ifPresent(legacy ->
                rolePermissionRepository.findByRoleId(legacy.getId())
                    .forEach(rp -> permissionNames.add(rp.getPermission().getPermissionName())));
        }
        grantPermissions(agentRole, permissionNames);

        for (String legacyName : List.of(Profiles.LEGACY_SAISIE, Profiles.LEGACY_VALIDATEUR)) {
            Role legacy = roleRepository.findByRoleName(legacyName).orElse(null);
            if (legacy == null) {
                continue;
            }
            for (UserRole assignment : userRoleRepository.findUsersByRoleId(legacy.getId())) {
                Long userId = assignment.getUser().getId();
                UserRole existing = userRoleRepository.findByUserAndRole(userId, agentRole.getId()).orElse(null);
                if (existing == null) {
                    UserRole migrated = new UserRole();
                    migrated.setUser(assignment.getUser());
                    migrated.setRole(agentRole);
                    migrated.setAssignedBy("SYSTEM_MIGRATION");
                    userRoleRepository.save(migrated);
                } else if (!existing.isActive()) {
                    existing.restore();
                    existing.setAssignedBy("SYSTEM_MIGRATION");
                    userRoleRepository.save(existing);
                }
                assignment.revoke("SYSTEM_MIGRATION");
                userRoleRepository.save(assignment);
                log.info("Migrated user {} from {} to {}", assignment.getUser().getUsername(), legacyName, Profiles.AGENT_ENROLEMENT);
            }
            if (Boolean.TRUE.equals(legacy.getIsActive())) {
                legacy.setIsActive(false);
                roleRepository.save(legacy);
                log.info("Deactivated legacy profile {}", legacyName);
            }
        }
    }

    private void reconcileAgentHabilitationProfile() {
        Role role = ensureRole(Profiles.AGENT_HABILITATION,
            "Agent d'habilitation - gestion des utilisateurs et attribution des rôles agent d'enrôlement / consultant");
        grantPermissions(role, AGENT_HABILITATION_PERMISSIONS);
    }

    private Role ensureRole(String roleName, String description) {
        return roleRepository.findByRoleName(roleName).orElseGet(() -> {
            log.info("Creating profile {}", roleName);
            return roleRepository.save(Role.builder()
                .roleName(roleName)
                .description(description)
                .isActive(true)
                .build());
        });
    }

    private void grantPermissions(Role role, List<String> permissionNames) {
        for (String permissionName : new java.util.LinkedHashSet<>(permissionNames)) {
            permissionRepository.findByPermissionName(permissionName).ifPresent(permission -> {
                if (rolePermissionRepository.findByRoleAndPermission(role.getId(), permission.getId()).isEmpty()) {
                    rolePermissionRepository.save(RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .build());
                }
            });
        }
    }

    /**
     * Create all permission records
     */
    private void createPermissions() {
        log.info("Creating permissions...");
        List<Permission> permissions = new ArrayList<>();

        // === PARTNER MANAGEMENT ===
        permissions.add(Permission.builder()
            .permissionName("PARTNER_CREATE")
            .endpoint("/v1/create/partners")
            .httpMethod("POST")
            .description("Create a new partner")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_LIST_ALL")
            .endpoint("/v1/read/partners")
            .httpMethod("GET")
            .description("List all partners")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_VIEW_OWN")
            .endpoint("/getpartner/*")
            .httpMethod("GET")
            .description("View partner by ID")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_UPDATE_OWN")
            .endpoint("/v1/update/partners")
            .httpMethod("PUT")
            .description("Update partner record")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_STATUS_CHANGE")
            .endpoint("/v1/partners/*/status")
            .httpMethod("PATCH")
            .description("Activate or deactivate a partner")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // === VALIDATION WORKFLOW ===
        permissions.add(Permission.builder()
            .permissionName("PARTNER_SUBMIT")
            .endpoint("/v1/validations/partners/*/submit")
            .httpMethod("POST")
            .description("Submit partner for validation")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_DRAFT")
            .endpoint("/v1/validations/partners/*/draft")
            .httpMethod("POST")
            .description("Save partner as draft")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_VALIDATE")
            .endpoint("/v1/validations/*/validate")
            .httpMethod("POST")
            .description("Validate a pending partner")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_REJECT")
            .endpoint("/v1/validations/*/reject")
            .httpMethod("POST")
            .description("Reject a pending partner")
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
            .permissionName("PARTNER_DRAFTS_LIST")
            .endpoint("/v1/validations/drafts")
            .httpMethod("GET")
            .description("List draft validations")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("PARTNER_VALIDATION_VIEW")
            .endpoint("/v1/validations/**")
            .httpMethod("GET")
            .description("View validation records")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // === EXPORT ===
        permissions.add(Permission.builder()
            .permissionName("PARTNER_EXPORT")
            .endpoint("/v1/export/partners")
            .httpMethod("GET")
            .description("Export partners to CSV")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // === ADMIN USER MANAGEMENT ===
        permissions.add(Permission.builder()
            .permissionName("ADMIN_USER_VIEW")
            .endpoint("/admin/users")
            .httpMethod("GET")
            .description("List all users")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_USER_VIEW_ONE")
            .endpoint("/admin/users/**")
            .httpMethod("GET")
            .description("View user details and roles")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_USER_MANAGE")
            .endpoint("/admin/users")
            .httpMethod("POST")
            .description("Create users")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_USER_UPDATE")
            .endpoint("/admin/users/*")
            .httpMethod("PUT")
            .description("Update user")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_USER_DELETE")
            .endpoint("/admin/users/*")
            .httpMethod("DELETE")
            .description("Delete user")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_ROLE_ASSIGN")
            .endpoint("/admin/users/*/roles")
            .httpMethod("POST")
            .description("Assign role to user")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_ROLE_REVOKE")
            .endpoint("/admin/users/*/roles/*")
            .httpMethod("DELETE")
            .description("Revoke role from user")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // === ADMIN ROLE MANAGEMENT ===
        permissions.add(Permission.builder()
            .permissionName("ADMIN_ROLE_MANAGE")
            .endpoint("/admin/roles")
            .httpMethod("POST")
            .description("Create and manage roles")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_ROLE_VIEW")
            .endpoint("/admin/roles/**")
            .httpMethod("GET")
            .description("List and view roles")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // === ADMIN PERMISSION MANAGEMENT ===
        permissions.add(Permission.builder()
            .permissionName("ADMIN_PERMISSION_MANAGE")
            .endpoint("/admin/permissions")
            .httpMethod("POST")
            .description("Create and manage permissions")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_PERMISSION_VIEW")
            .endpoint("/admin/permissions/**")
            .httpMethod("GET")
            .description("List and view permissions")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        permissions.add(Permission.builder()
            .permissionName("ADMIN_PERMISSION_UPDATE")
            .endpoint("/admin/permissions/*")
            .httpMethod("PUT")
            .description("Activate or deactivate permissions")
            .requiresOwnerCheck(false)
            .isActive(true)
            .build());

        // === AUDIT LOGS ===
        permissions.add(Permission.builder()
            .permissionName("AUDIT_LOG_VIEW")
            .endpoint("/admin/audit-logs")
            .httpMethod("GET")
            .description("View audit logs")
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
            "PARTNER_VIEW_OWN", "PARTNER_UPDATE_OWN", "PARTNER_STATUS_CHANGE",
            "PARTNER_SUBMIT", "PARTNER_DRAFT", "PARTNER_DRAFTS_LIST", "PARTNER_VALIDATION_VIEW");
        log.info("Created role: PROFIL_SAISIE");

        // Role 2: PROFIL_VALIDATEUR - Validator
        Role validateurRole = Role.builder()
            .roleName("PROFIL_VALIDATEUR")
            .description("Validator - Can view and validate partner records")
            .isActive(true)
            .build();
        roleRepository.save(validateurRole);

        assignPermissionsToRole(validateurRole, "PARTNER_LIST_ALL", "PARTNER_VALIDATE",
            "PARTNER_REJECT", "PARTNER_VALIDATION_LIST", "PARTNER_EXPORT",
            "PARTNER_VALIDATION_VIEW");
        log.info("Created role: PROFIL_VALIDATEUR");

        // Role 3: PROFIL_CONSULTATION - Consultation (Read-only access)
        Role consultationRole = Role.builder()
            .roleName("PROFIL_CONSULTATION")
            .description("Consultation - Read-only access to partner records")
            .isActive(true)
            .build();
        roleRepository.save(consultationRole);

        assignPermissionsToRole(consultationRole, "PARTNER_LIST_ALL",
            "PARTNER_VALIDATION_VIEW", "PARTNER_EXPORT");
        log.info("Created role: PROFIL_CONSULTATION");

        // Role 4: PROFIL_ADMIN_CENTRAL - Administration only
        Role adminRole = Role.builder()
            .roleName("PROFIL_ADMIN_CENTRAL")
            .description("Central Admin - User, role, permission and audit management")
            .isActive(true)
            .build();
        roleRepository.save(adminRole);

        reconcileAdminPermissions();
    }

    private void reconcileAdminPermissions() {
        Role adminRole = roleRepository.findByRoleName("PROFIL_ADMIN_CENTRAL").orElse(null);
        if (adminRole == null) {
            return;
        }

        List<RolePermission> existingAssignments = rolePermissionRepository.findByRoleId(adminRole.getId());
        for (RolePermission assignment : existingAssignments) {
            String permissionName = assignment.getPermission().getPermissionName();
            if (!permissionName.startsWith("ADMIN_") && !permissionName.equals("AUDIT_LOG_VIEW")) {
                rolePermissionRepository.delete(assignment);
            }
        }

        log.info("Reconciled PROFIL_ADMIN_CENTRAL permissions to administration and audit only");
    }

    private void reconcileAdminPermissionUpdate() {
        Role adminRole = roleRepository.findByRoleName("PROFIL_ADMIN_CENTRAL").orElse(null);
        Permission permission = permissionRepository.findByPermissionName("ADMIN_PERMISSION_UPDATE")
            .orElseGet(() -> permissionRepository.save(Permission.builder()
                .permissionName("ADMIN_PERMISSION_UPDATE")
                .endpoint("/admin/permissions/*")
                .httpMethod("PUT")
                .description("Activate or deactivate permissions")
                .requiresOwnerCheck(false)
                .isActive(true)
                .build()));

        if (adminRole != null
                && rolePermissionRepository.findByRoleAndPermission(adminRole.getId(), permission.getId()).isEmpty()) {
            rolePermissionRepository.save(RolePermission.builder()
                .role(adminRole)
                .permission(permission)
                .build());
        }
    }

    /**
     * Lets the central admin see aggregated partner counts on the dashboard
     * (no access to the partner records themselves).
     */
    private void reconcileAdminPartnerStatsPermission() {
        Role adminRole = roleRepository.findByRoleName("PROFIL_ADMIN_CENTRAL").orElse(null);
        Permission permission = permissionRepository.findByPermissionName("ADMIN_PARTNER_STATS")
            .orElseGet(() -> permissionRepository.save(Permission.builder()
                .permissionName("ADMIN_PARTNER_STATS")
                .endpoint("/admin/stats/partners")
                .httpMethod("GET")
                .description("View aggregated partner counts (dashboard)")
                .requiresOwnerCheck(false)
                .isActive(true)
                .build()));

        if (adminRole != null
                && rolePermissionRepository.findByRoleAndPermission(adminRole.getId(), permission.getId()).isEmpty()) {
            rolePermissionRepository.save(RolePermission.builder()
                .role(adminRole)
                .permission(permission)
                .build());
        }
    }

    private void reconcilePartnerExportPermission() {
        Permission exportPermission = permissionRepository.findByPermissionName("PARTNER_EXPORT").orElse(null);
        if (exportPermission == null) {
            log.warn("PARTNER_EXPORT permission not found");
            return;
        }

        for (String roleName : List.of("PROFIL_SAISIE", "PROFIL_VALIDATEUR", "PROFIL_CONSULTATION")) {
            Role role = roleRepository.findByRoleName(roleName).orElse(null);
            if (role == null || rolePermissionRepository.findByRoleAndPermission(role.getId(), exportPermission.getId()).isPresent()) {
                continue;
            }

            rolePermissionRepository.save(RolePermission.builder()
                .role(role)
                .permission(exportPermission)
                .build());
            log.info("Assigned PARTNER_EXPORT to {}", roleName);
        }
    }

    private void reconcileRemovedTransferPermissions() {
        permissionRepository.findAllForAdmin().stream()
            .filter(permission -> permission.getPermissionName().startsWith("TRANSFER_")
                    || permission.getPermissionName().equals("PARTNER_DELETE"))
            .filter(permission -> Boolean.TRUE.equals(permission.getIsActive()))
            .forEach(permission -> {
                permission.setIsActive(false);
                permissionRepository.save(permission);
            });
    }

    private void reconcilePartnerStatusPermission() {
        Permission permission = permissionRepository.findByPermissionName("PARTNER_STATUS_CHANGE")
            .orElseGet(() -> permissionRepository.save(Permission.builder()
                .permissionName("PARTNER_STATUS_CHANGE")
                .endpoint("/v1/partners/*/status")
                .httpMethod("PATCH")
                .description("Activate or deactivate a partner")
                .requiresOwnerCheck(false)
                .isActive(true)
                .build()));
        Role saisieRole = roleRepository.findByRoleName("PROFIL_SAISIE").orElse(null);
        if (saisieRole != null
                && rolePermissionRepository.findByRoleAndPermission(saisieRole.getId(), permission.getId()).isEmpty()) {
            rolePermissionRepository.save(RolePermission.builder()
                .role(saisieRole)
                .permission(permission)
                .build());
        }
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
