package edu.utec.planificador;

import edu.utec.planificador.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests.
 * 
 * <p>This abstract class provides common configuration and setup for all
 * integration tests in the application. By extending this class, test classes
 * automatically inherit:</p>
 * 
 * <ul>
 *   <li>Full Spring application context with all beans</li>
 *   <li>H2 in-memory database (PostgreSQL compatibility mode)</li>
 *   <li>Test-specific configuration (faster BCrypt, etc.)</li>
 *   <li>Transactional rollback after each test (clean state)</li>
 *   <li>SQL script execution for test data setup</li>
 * </ul>
 * 
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * class UserServiceIntegrationTest extends BaseIntegrationTest {
 *     {@code @Autowired}
 *     private UserService userService;
 *     
 *     {@code @Test}
 *     void shouldCreateUser() {
 *         // test code
 *     }
 * }
 * </pre>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li><strong>Transactional:</strong> Each test runs in a transaction that's rolled back</li>
 *   <li><strong>Isolated:</strong> Tests don't interfere with each other</li>
 *   <li><strong>Fast:</strong> In-memory database, optimized configurations</li>
 *   <li><strong>Repeatable:</strong> Clean state guaranteed before each test</li>
 * </ul>
 * 
 * @see SpringBootTest
 * @see ActiveProfiles
 * @see Transactional
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIntegrationTest {

    /**
     * Common setup executed before each test method.
     * 
     * <p>Override this method in subclasses to add custom setup logic.
     * Remember to call {@code super.setUp()} if you override this method.</p>
     */
    @BeforeEach
    public void setUp() {
        // Common setup logic for all integration tests
        // Can be overridden by subclasses
    }
    
    /**
     * Utility method for tests to pause execution (useful for debugging).
     * 
     * <p><strong>WARNING:</strong> Only use during debugging. Remove before committing.</p>
     * 
     * @param milliseconds time to sleep in milliseconds
     */
    protected void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
