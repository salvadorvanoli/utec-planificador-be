-- ============================================
-- Test Data Fixtures
-- ============================================
-- Common test data that can be loaded before tests.
-- Use with: @Sql("/test-data.sql")
-- ============================================

-- Test Campus
INSERT INTO campus (id, name, code, city, address, enabled) VALUES
(1, 'Campus Test Montevideo', 'CTM', 'Montevideo', 'Av. Test 1234', true),
(2, 'Campus Test Rivera', 'CTR', 'Rivera', 'Calle Test 5678', true);

-- Test Users will be inserted by individual tests as needed
-- to maintain test isolation and independence

-- Note: IDs are hardcoded for H2 in-memory database.
-- In a real scenario, you might use sequences or let JPA generate IDs.
