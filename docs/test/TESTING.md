# Testing Strategy - UTEC Planificador Backend

## Overview

This project follows industry best practices for testing Spring Boot applications. Tests are isolated, fast, and do not require external infrastructure.

## Testing Architecture

### Test Database: H2 In-Memory

We use **H2 Database** in **PostgreSQL compatibility mode** for all tests:

**Advantages:**
- **Fast**: In-memory, no disk I/O
- **Isolated**: Each test run gets a fresh database
- **No Setup**: No need to run PostgreSQL for tests
- **CI/CD Friendly**: Works in any environment
- **Repeatable**: Guaranteed clean state

### Configuration Files

```
src/test/resources/
├── application-test.yml    # Test-specific Spring configuration
├── cleanup.sql            # Pre-test cleanup script
└── test-data.sql          # Common test fixtures
```

### Test Classes Structure

```
src/test/java/
├── BaseIntegrationTest.java          # Base class for integration tests
├── config/
│   └── TestConfig.java               # Test-specific bean configurations
└── edu/utec/planificador/
    └── UtecPlanificadorDocenteBackendApplicationTests.java
```

## Running Tests

### Run All Tests
```bash
./gradlew test
```

### Run Tests with Build
```bash
./gradlew clean build
```

### Run Tests Without Build (faster)
```bash
./gradlew test --rerun-tasks
```

### Run Specific Test Class
```bash
./gradlew test --tests "UtecPlanificadorDocenteBackendApplicationTests"
```

### Run Tests with Detailed Output
```bash
./gradlew test --info
```

### Run Tests and Generate Coverage Report
```bash
./gradlew test jacocoTestReport
```

## Writing Tests

### Integration Tests (Recommended)

Extend `BaseIntegrationTest` for full Spring context tests:

```java
@Sql("/test-data.sql") // Optional: Load test fixtures
class UserServiceIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = new User("test@utec.edu.uy", "password");
        
        // When
        User saved = userService.save(user);
        
        // Then
        assertThat(saved.getId()).isNotNull();
    }
}
```

### Unit Tests (For Service Logic)

For testing business logic without database:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldFindUserByEmail() {
        // Given
        when(userRepository.findByEmail("test@utec.edu.uy"))
            .thenReturn(Optional.of(new User()));
        
        // When
        Optional<User> user = userService.findByEmail("test@utec.edu.uy");
        
        // Then
        assertThat(user).isPresent();
    }
}
```

### Controller Tests (REST API)

Use `@WebMvcTest` for testing controllers in isolation:

```java
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    @WithMockUser(roles = "EDUCATION_MANAGER")
    void shouldGetUserById() throws Exception {
        // Given
        when(userService.findById(1L))
            .thenReturn(Optional.of(new User()));
        
        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

## Test Configuration Details

### H2 Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop  # Fresh schema for each test class
```

### Key Features

1. **PostgreSQL Compatibility Mode**: Ensures H2 behaves like PostgreSQL
2. **DATABASE_TO_LOWER=TRUE**: Matches PostgreSQL's lowercase table names
3. **create-drop**: Clean schema before/after each test class
4. **Fast BCrypt (strength 4)**: 256x faster than production (strength 12)

## Test Categories

### 1. Context Load Tests
- **Purpose**: Verify Spring context loads successfully
- **File**: `UtecPlanificadorDocenteBackendApplicationTests`
- **Speed**: ~5-10 seconds

### 2. Integration Tests
- **Purpose**: Test multiple components together
- **Extend**: `BaseIntegrationTest`
- **Database**: H2 in-memory
- **Transactions**: Auto-rollback after each test

### 3. Unit Tests
- **Purpose**: Test individual components in isolation
- **Mocking**: Mockito
- **No Database**: Pure logic testing

### 4. Controller Tests
- **Purpose**: Test REST endpoints
- **Framework**: MockMvc
- **Security**: Mock authentication

## Best Practices

### DO

- Use `@ActiveProfiles("test")` for all tests
- Extend `BaseIntegrationTest` for integration tests
- Use `@Transactional` for automatic rollback
- Keep tests independent (no shared state)
- Use descriptive test method names (`shouldCreateUserWhenValidData`)
- Follow AAA pattern: Arrange, Act, Assert

### DON'T

- Don't require external databases for tests
- Don't use `@SpringBootTest` for unit tests (too slow)
- Don't share state between tests
- Don't use real external services (use mocks/stubs)
- Don't commit with failing tests
- Don't test framework code (only your business logic)

## Troubleshooting

### Tests Fail with "Connection Refused"
**Solution**: You're not using the test profile. Add `@ActiveProfiles("test")` to your test class.

### Tests Are Too Slow
**Solution**: 
1. Use `@WebMvcTest` instead of `@SpringBootTest` for controller tests
2. Use unit tests with mocks instead of integration tests
3. Verify BCrypt strength is set to 4 in TestConfig

### H2 Compatibility Issues
**Solution**: Most PostgreSQL features work in H2's PostgreSQL mode. For advanced features (JSON, arrays), you may need to:
1. Use conditional SQL in tests
2. Use Testcontainers with real PostgreSQL (slower but accurate)

### "Table Not Found" Errors
**Solution**: 
1. Verify `ddl-auto: create-drop` is set in `application-test.yml`
2. Check entity annotations are correct
3. Ensure H2 is on test classpath

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Additional Resources

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)

## Maintenance

### Updating Test Dependencies

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.springframework.security:spring-security-test'
testRuntimeOnly 'com.h2database:h2'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

### Adding New Test Fixtures

1. Add SQL to `src/test/resources/test-data.sql`
2. Reference in test with `@Sql("/test-data.sql")`
3. Data is loaded before test, cleaned after (transactional rollback)

---

**Last Updated**: October 28, 2025  
**Maintained By**: UTEC Development Team
