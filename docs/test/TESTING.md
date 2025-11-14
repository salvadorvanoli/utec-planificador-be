# Testing Strategy - UTEC Planificador Backend

> **Last Update**: November 14, 2025  
> **Status**: ✅ 46 unit tests working

## Overview

This project implements a comprehensive **unit testing strategy** for Spring Boot applications using JUnit 5 and Mockito. Tests are fast, isolated, and don't require external infrastructure.

## Testing Approach

### Focus: Unit Testing

We focus on **unit tests** with mocks instead of integration tests with real databases:

**Advantages:**
- ⚡ **Fast**: No database initialization, runs in ~15 seconds
- 🔒 **Isolated**: Each test is completely independent
- 🚀 **Simple**: No complex setup or infrastructure
- ✅ **CI/CD Friendly**: Works in any environment
- 🎯 **Focused**: Tests specific business logic

### Current State

- ✅ **46 unit tests** implemented and passing
- ✅ Services coverage (29 tests)
- ✅ Utilities coverage (10 tests)  
- ✅ Generators coverage (7 tests)
- ❌ Controller tests (not implemented due to Spring Security complexity)

## Test Structure

```
src/test/java/edu/utec/planificador/
├── service/                              29 tests ✅
│   ├── AuthenticationServiceTest.java    5 tests
│   ├── CampusServiceTest.java            4 tests
│   ├── EnumServiceTest.java              13 tests
│   ├── UserPositionServiceTest.java      5 tests
│   └── WeeklyPlanningGeneratorTest.java  7 tests
├── util/                                 10 tests ✅
│   ├── CookieUtilTest.java               3 tests
│   └── EnumUtilsTest.java                7 tests
└── UtecPlanificadorDocenteBackendApplicationTests.java  1 test ✅
```

## Running Tests

### Run All Tests
```bash
# Windows
.\gradlew test

# Linux/Mac
./gradlew test
```

### Expected Output
```
> Task :test

BUILD SUCCESSFUL in 15s
46 tests completed, 46 passed
```

### Run Tests with Coverage
```bash
.\gradlew test jacocoTestReport
```

### View Reports
```bash
# Windows
start build\reports\tests\test\index.html
start build\reports\jacoco\test\html\index.html
```

### Run Specific Test Class
```bash
.\gradlew test --tests "CampusServiceTest"
```

### Run Tests with Detailed Output
```bash
.\gradlew test --info
```

## Writing Tests

### Unit Test Structure

All tests follow the **Given-When-Then** pattern:

```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CampusService Unit Tests")
class CampusServiceTest {
    
    @Mock
    private CampusRepository campusRepository;
    
    @Mock
    private CampusMapper campusMapper;
    
    @InjectMocks
    private CampusServiceImpl campusService;
    
    @Test
    @DisplayName("Should return all campuses when userId is null")
    void getCampuses_WithoutUserId_ReturnsAllCampuses() {
        // Given
        List<Campus> campuses = List.of(createTestCampus());
        when(campusRepository.findAll()).thenReturn(campuses);
        when(campusMapper.toResponse(any())).thenReturn(createCampusResponse());
        
        // When
        List<CampusResponse> result = campusService.getCampuses(null);
        
        // Then
        assertThat(result).isNotEmpty();
        verify(campusRepository, times(1)).findAll();
    }
    
    private Campus createTestCampus() {
        Campus campus = mock(Campus.class);
        when(campus.getId()).thenReturn(1L);
        when(campus.getName()).thenReturn("Test Campus");
        return campus;
    }
}
```

### Key Patterns Used

#### 1. Mockito for Dependencies
```java
@Mock
private UserRepository userRepository;

@InjectMocks
private UserServiceImpl userService;
```

#### 2. Lenient Strictness
```java
@MockitoSettings(strictness = Strictness.LENIENT)
```
Allows optional stubs in `@BeforeEach` setup.

#### 3. AssertJ Assertions
```java
assertThat(result)
    .isNotNull()
    .extracting(CampusResponse::getName)
    .isEqualTo("Test Campus");
```

#### 4. Lombok Builder for DTOs
```java
CampusResponse response = CampusResponse.builder()
    .id(1L)
    .name("Test Campus")
    .build();
```

#### 5. Mock for Entities
```java
// Entities with protected constructors
Campus campus = mock(Campus.class);
when(campus.getId()).thenReturn(1L);
```

## Test Categories

### 1. Context Load Test (1 test)
- **File**: `UtecPlanificadorDocenteBackendApplicationTests`
- **Purpose**: Verify Spring context loads successfully

### 2. Service Unit Tests (29 tests)
- **Purpose**: Test business logic in isolation
- **Mocks**: Repositories, mappers, external services
- **Files**: `*ServiceTest.java`

### 3. Utility Tests (10 tests)
- **Purpose**: Test helper classes and utilities
- **Files**: `*UtilTest.java`

### 4. Generator Tests (7 tests)
- **Purpose**: Test data generation logic
- **Files**: `*GeneratorTest.java`
## Best Practices

### ✅ DO

- ✅ Use `@ExtendWith(MockitoExtension.class)` for unit tests
- ✅ Use `@MockitoSettings(strictness = Strictness.LENIENT)` when needed
- ✅ Keep tests independent (no shared state)
- ✅ Use descriptive `@DisplayName` annotations
- ✅ Follow **Given-When-Then** pattern
- ✅ Mock entities with protected constructors using `mock()`
- ✅ Use `@Builder` for DTOs in tests
- ✅ Verify important interactions with `verify()`

### ❌ DON'T

- ❌ Don't use real databases in unit tests
- ❌ Don't share mutable state between tests
- ❌ Don't mock the class under test
- ❌ Don't commit with failing tests
- ❌ Don't test framework code
- ❌ Don't create unnecessary test data

## Troubleshooting

### UnnecessaryStubbingException
**Problem**: Mockito complains about unused stubs.  
**Solution**: Add `@MockitoSettings(strictness = Strictness.LENIENT)`

### Cannot Instantiate Entity with Protected Constructor
**Problem**: Lombok `@AllArgsConstructor` with `access = AccessLevel.PROTECTED`  
**Solution**: Use `mock()` instead of `new`:
```java
Campus campus = mock(Campus.class);
when(campus.getId()).thenReturn(1L);
```

### EntityManagerFactory Errors in @WebMvcTest
**Problem**: Spring tries to load JPA even with `excludeAutoConfiguration`  
**Solution**: Use unit tests with Mockito instead, or `@SpringBootTest` (slower)

## CI/CD Integration

### GitHub Actions

Tests run automatically on:
- ✅ Push to `main`
- ✅ Pull Requests
- ✅ Feature branches

**Configuration**: `.github/workflows/backend-ci.yml`

```yaml
name: Backend CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Java 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests
        run: ./gradlew test
      - name: Generate coverage
        run: ./gradlew jacocoTestReport
```

## Coverage Reports

### Generate Report
```bash
.\gradlew test jacocoTestReport
```

### View Report
- **HTML**: `build/reports/jacoco/test/html/index.html`
- **XML**: `build/reports/jacoco/test/jacocoTestReport.xml`

### Minimum Coverage
- **Required**: 60%
- **Recommended**: 80%

## Future Improvements

### Potential Additions

1. **Integration Tests** with `@SpringBootTest` and H2
2. **E2E Tests** with REST Assured
3. **Mutation Testing** with Pitest
4. **Performance Tests** for critical paths
5. **Contract Tests** for APIs

### Not Currently Implemented

- ❌ Controller tests (`@WebMvcTest` complexity)
- ❌ Repository tests (Mockito covers business logic)
- ❌ E2E tests (manual testing with Swagger)

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Guide](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

---

**Last Updated**: November 14, 2025  
**Version**: 1.0  
**Status**: ✅ 46 Tests Passing

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
