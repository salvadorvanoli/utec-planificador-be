package edu.utec.planificador.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Test-specific Spring configuration.
 * 
 * <p>This configuration class provides beans optimized for testing environments.
 * It overrides production beans with test-friendly implementations that are
 * faster and more predictable.</p>
 * 
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Weaker BCrypt strength (4) for faster password encoding in tests</li>
 *   <li>Can be extended with mock beans for external services</li>
 *   <li>Test-specific configurations that don't affect production code</li>
 * </ul>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * {@code @SpringBootTest}
 * {@code @ActiveProfiles("test")}
 * {@code @Import(TestConfig.class)}
 * class MyTest {
 *     // test code
 * }
 * </pre>
 * 
 * @see org.springframework.boot.test.context.TestConfiguration
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides a test-optimized BCrypt password encoder.
     * 
     * <p>Uses strength 4 instead of production's 12 for significantly faster
     * password encoding/verification during tests. This is acceptable because
     * test security requirements are minimal.</p>
     * 
     * <p><strong>Performance:</strong> Strength 4 is ~256x faster than strength 12</p>
     * 
     * @return password encoder optimized for test performance
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        // Use lower strength for faster test execution
        // Production uses strength 12, tests use strength 4
        return new BCryptPasswordEncoder(4);
    }
}
