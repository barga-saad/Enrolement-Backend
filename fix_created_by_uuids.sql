-- =======================================================
-- SQL Cleanup Script: Fix UUID-based createdBy values
-- Run after SecurityUtils was fixed to store usernames.
-- Old records created before the fix have UUIDs instead
-- of usernames in created_by / validated_by columns.
-- =======================================================

-- Preview affected records before updating
-- SELECT id, created_by FROM partners WHERE created_by ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Fix partners table: replace UUID with placeholder
UPDATE partners
SET created_by = 'migrated_user'
WHERE created_by ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Fix validation_records table: replace UUID creator
UPDATE validation_records
SET created_by = 'migrated_user'
WHERE created_by ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Fix validation_records: replace UUID validator
UPDATE validation_records
SET validated_by = 'migrated_user'
WHERE validated_by IS NOT NULL
  AND validated_by ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Fix audit_logs table if it exists
UPDATE audit_logs
SET username = 'migrated_user'
WHERE username ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

-- Verify: should return 0 rows after cleanup
-- SELECT count(*) FROM partners WHERE created_by ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
-- SELECT count(*) FROM validation_records WHERE created_by ~ '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';