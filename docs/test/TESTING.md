# Testing Strategy - UTEC Planificador Backend
> **Last Update**: November 14, 2025  
> **Status**: ✅ 49 tests working (30+ integration tests, 19 unit tests)

## Overview

This project implements a comprehensive **testing strategy** combining unit tests and integration tests for Spring Boot applications using JUnit 5, Mockito, and MockMvc.

## Testing Approach

### Mixed Testing Strategy

We use both **unit tests** with mocks and **integration tests** for controllers:

**Unit Tests Advantages:**
- ⚡ **Fast**: No database initialization, runs quickly
- 🔒 **Isolated**: Each test is completely independent
- 🚀 **Simple**: No complex setup or infrastructure
- ✅ **CI/CD Friendly**: Works in any environment
- 🎯 **Focused**: Tests specific business logic

### Current State

**Integration Tests Advantages:**
- 🔗 **Realistic**: Tests actual HTTP endpoints
- 🛡️ **Security**: Validates authentication and authorization
- 📝 **Documentation**: Shows real API usage examples

**Test Coverage:**
- ✅ **49 unit tests** implemented and passing
- ✅ Utilities coverage (10 tests)  
- ✅ Generators coverage (7 tests)
- ✅ **Controller integration tests** implemented:
  - ✅ AuthControllerIntegrationTest (empty - pendiente)
  - ✅ CampusControllerIntegrationTest (2 tests)
  - ✅ UserControllerIntegrationTest (empty - pendiente)
  - ✅ EnumControllerIntegrationTest (13 tests)
  - ✅ ActivityControllerIntegrationTest (4 tests)
  - ✅ CourseControllerIntegrationTest (4 tests)
  - ✅ CurricularUnitControllerIntegrationTest (4 tests)
  - ✅ RegionalTechnologicalInstituteControllerIntegrationTest (3 tests)
## Test Structure

```
src/test/java/edu/utec/planificador/
├── controller/                           30+ tests ✅
│   ├── AuthControllerIntegrationTest.java       (pendiente)
│   ├── CampusControllerIntegrationTest.java     2 tests
│   ├── UserControllerIntegrationTest.java       (pendiente)
│   ├── EnumControllerIntegrationTest.java       13 tests
│   ├── ActivityControllerIntegrationTest.java   4 tests
│   ├── CourseControllerIntegrationTest.java     4 tests
│   ├── CurricularUnitControllerIntegrationTest.java  4 tests
│   └── RegionalTechnologicalInstituteControllerIntegrationTest.java  3 tests
├── service/                              29 tests ✅
│   ├── AuthenticationServiceTest.java           5 tests
│   ├── CampusServiceTest.java                   4 tests
│   ├── EnumServiceTest.java                     13 tests
│   ├── UserPositionServiceTest.java             5 tests
│   └── WeeklyPlanningGeneratorTest.java         7 tests
├── util/                                 10 tests ✅
│   ├── CookieUtilTest.java                      3 tests
│   └── EnumUtilsTest.java                       7 tests
├── config/
│   └── TestSecurityConfig.java          (configuración de seguridad para tests)
├── BaseIntegrationTest.java             (clase base para tests de integración)
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
49 tests completed, 49 passed
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

### 5. Controller Integration Tests (27+ tests)
- **Purpose**: Test HTTP endpoints with MockMvc
- **Coverage**: Authentication, authorization, request/response validation
- **Files**: `*ControllerIntegrationTest.java`
- **Technology**: `@SpringBootTest`, `@AutoConfigureMockMvc`, `MockMvc`

#### Controller Test Structure

```java
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("EnumController Integration Tests")
class EnumControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private EnumService enumService;
    
    @Test
    @DisplayName("GET /enums - Should return all enumerations")
    void getAllEnums_ReturnsAllEnumerations() throws Exception {
        // Given
        Map<String, List<EnumResponse>> allEnums = Map.of(
            "roles", List.of(new EnumResponse("TEACHER", "Docente"))
        );
        when(enumService.getAllEnums()).thenReturn(allEnums);
        
        // When & Then
        mockMvc.perform(get("/enums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
        
        verify(enumService, times(1)).getAllEnums();
    }
    
    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", authorities = "COURSE_WRITE")
    @DisplayName("POST /courses - Should create course with proper permissions")
    void createCourse_WithPermissions_CreatesCourse() throws Exception {
        String json = """
                {
                    "description": "Nuevo curso"
                }
                """;
        
        mockMvc.perform(post("/courses")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());
    }
}
```

#### Key Patterns for Integration Tests

1. **MockMvc for HTTP Simulation**
   ```java
   mockMvc.perform(get("/api/endpoint"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.field").value("value"));
   ```

2. **Security Testing with @WithMockUser**
   ```java
   @WithMockUser(username = "user@test.com", authorities = "READ_PRIVILEGE")
   void testSecuredEndpoint() { ... }
   ```

3. **Mock Services, Not Controllers**
   ```java
   @MockitoBean
   private EnumService enumService;
   // Controller is real, service is mocked
   ```

4. **Test Security Config**
   ```java
   @Import(TestSecurityConfig.class)
   // Provides mock JWT validation for tests
   ```

#### Public Endpoints Configuration

Some endpoints are publicly accessible without authentication. These must be configured in `SecurityConfig.java`:

```java
private static final String[] PUBLIC_GET_ENDPOINTS = {
    "/users/teachers",
    "/campuses",
    "/courses",
    "/regional-technological-institutes"  // Added for RTI endpoint
};
```

**Example Test for Public Endpoint:**
```java
@Test
@DisplayName("GET /regional-technological-institutes - Should return all RTIs without authentication")
void getRegionalTechnologicalInstitutes_WithoutUserId_ReturnsAllRTIs() throws Exception {
    // Given
    List<RegionalTechnologicalInstituteResponse> rtis = List.of(
        RegionalTechnologicalInstituteResponse.builder()
            .id(1L)
            .name("ITR Norte")
            .build()
    );
    when(regionalTechnologicalInstituteService.getRegionalTechnologicalInstitutes(null))
        .thenReturn(rtis);
    
    // When & Then
    mockMvc.perform(get("/regional-technological-institutes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
}
```

> **Note**: When adding new public endpoints, remember to update both `SecurityConfig.java` and document them in the API.

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
**Version**: 1.1  
**Status**: ✅ 49 Tests Passing

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
