-- ============================================================
-- SQL SCRIPT TO CREATE TEST USERS FOR ENROLLMENT SYSTEM
-- ============================================================
-- Database: enrollmentdb
-- Note: Execute these queries in PostgreSQL

-- ============================================================
-- 1. CREATE ROLES (if they don't exist)
-- ============================================================

INSERT INTO roles (role_name, description, is_active, created_at, updated_at, deleted_at)
VALUES 
  ('ADMIN', 'System Administrator - Full Access', true, NOW(), NOW(), NULL),
  ('VALIDATEUR', 'Validator - Review and Approve Transactions', true, NOW(), NOW(), NULL),
  ('SAISIE', 'Data Entry - Can Create and View Partners', true, NOW(), NOW(), NULL),
  ('CONSULTATION', 'Consultant - View Only Access', true, NOW(), NOW(), NULL)
ON CONFLICT (role_name) DO NOTHING;

-- ============================================================
-- 2. CREATE TEST USERS
-- ============================================================

-- USER 1: ADMIN USER
INSERT INTO users (username, email, first_name, last_name, is_active, created_at, updated_at, deleted_at, deleted_by)
VALUES 
  ('admin_user', 'admin@bcp.ma', 'Admin', 'User', true, NOW(), NOW(), NULL, NULL)
ON CONFLICT (username) DO NOTHING;

-- USER 2: VALIDATOR USER
INSERT INTO users (username, email, first_name, last_name, is_active, created_at, updated_at, deleted_at, deleted_by)
VALUES 
  ('validateur_user', 'validateur@bcp.ma', 'John', 'Validator', true, NOW(), NOW(), NULL, NULL)
ON CONFLICT (username) DO NOTHING;

-- USER 3: DATA ENTRY USER
INSERT INTO users (username, email, first_name, last_name, is_active, created_at, updated_at, deleted_at, deleted_by)
VALUES 
  ('saisie_user', 'saisie@bcp.ma', 'Sarah', 'DataEntry', true, NOW(), NOW(), NULL, NULL)
ON CONFLICT (username) DO NOTHING;

-- USER 4: CONSULTANT USER
INSERT INTO users (username, email, first_name, last_name, is_active, created_at, updated_at, deleted_at, deleted_by)
VALUES 
  ('consultant_user', 'consultant@bcp.ma', 'Maria', 'Consultant', true, NOW(), NOW(), NULL, NULL)
ON CONFLICT (username) DO NOTHING;

-- ============================================================
-- 3. ASSIGN ROLES TO USERS (user_roles table)
-- ============================================================

-- ADMIN USER gets ADMIN role
INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by, deleted_at, deleted_by)
SELECT u.id, r.id, NOW(), 'system', NULL, NULL
FROM users u, roles r
WHERE u.username = 'admin_user' AND r.role_name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id AND ur.deleted_at IS NULL);

-- VALIDATOR USER gets VALIDATEUR role
INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by, deleted_at, deleted_by)
SELECT u.id, r.id, NOW(), 'system', NULL, NULL
FROM users u, roles r
WHERE u.username = 'validateur_user' AND r.role_name = 'VALIDATEUR'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id AND ur.deleted_at IS NULL);

-- DATA ENTRY USER gets SAISIE role
INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by, deleted_at, deleted_by)
SELECT u.id, r.id, NOW(), 'system', NULL, NULL
FROM users u, roles r
WHERE u.username = 'saisie_user' AND r.role_name = 'SAISIE'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id AND ur.deleted_at IS NULL);

-- CONSULTANT USER gets CONSULTATION role
INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by, deleted_at, deleted_by)
SELECT u.id, r.id, NOW(), 'system', NULL, NULL
FROM users u, roles r
WHERE u.username = 'consultant_user' AND r.role_name = 'CONSULTATION'
AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id AND ur.deleted_at IS NULL);

-- ============================================================
-- 4. ASSIGN PERMISSIONS TO ROLES (role_permissions table)
-- ============================================================

-- ADMIN ROLE PERMISSIONS (All permissions)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_name = 'ADMIN'
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- VALIDATEUR ROLE PERMISSIONS (Validation & Review permissions)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_name = 'VALIDATEUR'
AND p.permission_name IN (
  'VIEW_PARTNERS',
  'VALIDATE_PARTNERS',
  'APPROVE_PARTNERS',
  'VIEW_USERS',
  'VIEW_TRANSFERS'
)
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- SAISIE ROLE PERMISSIONS (Create & View permissions)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_name = 'SAISIE'
AND p.permission_name IN (
  'VIEW_PARTNERS',
  'CREATE_PARTNERS',
  'EDIT_PARTNERS',
  'VIEW_TRANSFERS',
  'CREATE_TRANSFERS'
)
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- CONSULTATION ROLE PERMISSIONS (View only)
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.role_name = 'CONSULTATION'
AND p.permission_name IN (
  'VIEW_PARTNERS',
  'VIEW_TRANSFERS',
  'VIEW_USERS'
)
AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

-- ============================================================
-- 5. VERIFY USER CREATION
-- ============================================================

SELECT 'USERS CREATED' as section;
SELECT u.id, u.username, u.email, u.first_name, u.last_name, u.is_active
FROM users u
WHERE u.username IN ('admin_user', 'validateur_user', 'saisie_user', 'consultant_user')
AND u.deleted_at IS NULL;

SELECT 'USER-ROLE ASSIGNMENTS' as section;
SELECT u.username, r.role_name
FROM user_roles ur
JOIN users u ON ur.user_id = u.id
JOIN roles r ON ur.role_id = r.id
WHERE u.username IN ('admin_user', 'validateur_user', 'saisie_user', 'consultant_user')
AND u.deleted_at IS NULL
AND ur.deleted_at IS NULL;

SELECT 'ROLE-PERMISSION COUNTS' as section;
SELECT r.role_name, COUNT(p.id) as permission_count
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.role_name IN ('ADMIN', 'VALIDATEUR', 'SAISIE', 'CONSULTATION')
GROUP BY r.role_name;
