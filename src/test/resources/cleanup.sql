-- ============================================
-- Test Data Cleanup Script
-- ============================================
-- This script is executed BEFORE each test method to ensure
-- a clean database state. It's referenced in @Sql annotation
-- in BaseIntegrationTest.
--
-- H2 database automatically creates tables via JPA (ddl-auto=create-drop),
-- but this script can be used to:
-- 1. Clear any residual data from previous tests
-- 2. Reset sequences/auto-increment counters
-- 3. Insert common test data fixtures
-- ============================================

-- Note: With ddl-auto=create-drop, tables are recreated for each test class,
-- so this cleanup is typically not needed. However, it's here as a template
-- for scenarios where you want finer control over test data.

-- Example: If you need to reset sequences (H2 syntax)
-- ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- Example: If you need to insert test fixtures
-- INSERT INTO campus (id, name, city) VALUES (1, 'Test Campus', 'Montevideo');
