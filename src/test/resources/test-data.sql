-- ============================================
-- Test Data Fixtures
-- ============================================
-- Common test data that can be loaded before tests.
-- Use with: @Sql("/test-data.sql")
-- ============================================

-- Test Regional Technological Institute
INSERT INTO regional_technological_institute (id, name) VALUES
(1, 'ITR Test Centro Sur');

-- Test Campus
INSERT INTO campus (id, name, code, city, address, enabled, regional_technological_institute_id) VALUES
(1, 'Campus Test Montevideo', 'CTM', 'Montevideo', 'Av. Test 1234', true, 1),
(2, 'Campus Test Rivera', 'CTR', 'Rivera', 'Calle Test 5678', true, 1);

-- Test Users will be inserted by individual tests as needed
-- to maintain test isolation and independence

-- Note: IDs are hardcoded for H2 in-memory database.
-- In a real scenario, you might use sequences or let JPA generate IDs.
