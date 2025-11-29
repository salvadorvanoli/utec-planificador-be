# Testing Strategy - UTEC Planificador Backend
> **Last Update**: November 26, 2025  
> **Status**: ✅ 89 tests working (39 integration tests, 50 unit tests)

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
- ✅ **Full Coverage**: Tests all REST controllers with MockMvc

**Test Coverage:**
- ✅ **89 tests total** implemented and passing
- ✅ **39 integration tests** (controllers)
- ✅ **50 unit tests** (services, utilities, generators)

**Controller Integration Tests:**
- ✅ ActivityControllerIntegrationTest (2 tests)
- ✅ AuthControllerIntegrationTest (6 tests)
- ✅ CampusControllerIntegrationTest (4 tests)
- ✅ CourseControllerIntegrationTest (2 tests)
- ✅ CurricularUnitControllerIntegrationTest (1 test)
- ✅ EnumControllerIntegrationTest (13 tests)
- ✅ RegionalTechnologicalInstituteControllerIntegrationTest (3 tests)
- ✅ UserControllerIntegrationTest (8 tests)

**Service Unit Tests:**
- ✅ AuthenticationServiceTest (5 tests)
- ✅ CampusServiceTest (4 tests)
- ✅ EnumServiceTest (14 tests)
- ✅ RegionalTechnologicalInstituteServiceTest (4 tests)
- ✅ UserPositionServiceTest (5 tests)

**Utility & Helper Tests:**
- ✅ CookieUtilTest (3 tests)
- ✅ EnumUtilsTest (7 tests)
- ✅ WeeklyPlanningGeneratorTest (7 tests)

**Application Test:**
- ✅ UtecPlanificadorDocenteBackendApplicationTests (1 test)
## Test Structure

```
src/test/java/edu/utec/planificador/
├── controller/                                                    39 tests ✅
│   ├── ActivityControllerIntegrationTest.java                     2 tests
│   ├── AuthControllerIntegrationTest.java                         6 tests
│   ├── CampusControllerIntegrationTest.java                       4 tests
│   ├── CourseControllerIntegrationTest.java                       2 tests
│   ├── CurricularUnitControllerIntegrationTest.java              1 test
│   ├── EnumControllerIntegrationTest.java                        13 tests
│   ├── RegionalTechnologicalInstituteControllerIntegrationTest.java  3 tests
│   └── UserControllerIntegrationTest.java                         8 tests
├── service/                                                       32 tests ✅
│   ├── AuthenticationServiceTest.java                             5 tests
│   ├── CampusServiceTest.java                                     4 tests
│   ├── EnumServiceTest.java                                      14 tests
│   ├── RegionalTechnologicalInstituteServiceTest.java            4 tests
│   └── UserPositionServiceTest.java                               5 tests
├── util/                                                          17 tests ✅
│   ├── CookieUtilTest.java                                        3 tests
│   ├── EnumUtilsTest.java                                         7 tests
│   └── WeeklyPlanningGeneratorTest.java                           7 tests
├── config/
│   └── TestSecurityConfig.java                    (configuración de seguridad para tests)
├── BaseIntegrationTest.java                       (clase base abstracta para tests de integración)
├── BaseSecurityTest.java                          (clase base abstracta con utilidades de seguridad)
└── UtecPlanificadorDocenteBackendApplicationTests.java            1 test ✅
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

BUILD SUCCESSFUL in 47s
89 tests completed, 89 passed, 0 failed, 0 skipped
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

### 2. Service Unit Tests (32 tests)
- **Purpose**: Test business logic in isolation
- **Mocks**: Repositories, mappers, external services
- **Files**: `*ServiceTest.java`
- **Covers**: 
  - AuthenticationService (login, security, blocked accounts)
  - CampusService (filtering by user)
  - EnumService (all enumeration types)
  - RegionalTechnologicalInstituteService (RTI filtering)
  - UserPositionService (user positions and roles)

### 3. Utility Tests (17 tests)
- **Purpose**: Test helper classes and utilities
- **Files**: `*UtilTest.java`, `*GeneratorTest.java`
- **Covers**:
  - CookieUtil (JWT cookie management)
  - EnumUtils (enum conversions and lookups)
  - WeeklyPlanningGenerator (date calculations and planning generation)

### 4. Controller Integration Tests (39 tests)
- **Purpose**: Test HTTP endpoints with MockMvc
- **Coverage**: Authentication, authorization, request/response validation
- **Files**: `*ControllerIntegrationTest.java`
- **Technology**: `@SpringBootTest`, `@AutoConfigureMockMvc`, `MockMvc`
- **Controllers Tested**:
  - ActivityController (CRUD operations)
  - AuthController (login, current user)
  - CampusController (campus listing and filtering)
  - CourseController (course management)
  - CurricularUnitController (curricular units)
  - EnumController (all enumeration endpoints)
  - RegionalTechnologicalInstituteController (RTI management)
  - UserController (user management, teachers, positions)

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

## Detailed Test Suites

### Controller Integration Tests (39 tests)

#### ActivityControllerIntegrationTest (2 tests)
- ✅ GET /activities/{id} - Should return activity by ID
- ✅ DELETE /activities/{id} - Should delete activity

#### AuthControllerIntegrationTest (6 tests)
- ✅ POST /auth/login - Should login successfully with valid credentials
- ✅ POST /auth/login - Should return 400 when email is null
- ✅ POST /auth/login - Should return 400 when email format is invalid
- ✅ POST /auth/login - Should return 400 when password is null
- ✅ GET /auth/me - Should return current user when authenticated
- ✅ GET /auth/me - Should return 401 when not authenticated

#### CampusControllerIntegrationTest (4 tests)
- ✅ GET /campuses - Should return all campuses without authentication
- ✅ GET /campuses - Should return campuses filtered by authenticated user
- ✅ GET /campuses?userId=1 - Should return campuses for specific user
- ✅ GET /campuses - Should return empty list when no campuses found

#### CourseControllerIntegrationTest (2 tests)
- ✅ GET /courses/{id} - Should return course by ID
- ✅ DELETE /courses/{id} - Should delete course

#### CurricularUnitControllerIntegrationTest (1 test)
- ✅ GET /curricular-units/{id} - Should return curricular unit by ID

#### EnumControllerIntegrationTest (13 tests)
- ✅ GET /enums - Should return all enumerations
- ✅ GET /enums/domain-areas - Should return domain areas
- ✅ GET /enums/professional-competencies - Should return professional competencies
- ✅ GET /enums/transversal-competencies - Should return transversal competencies
- ✅ GET /enums/cognitive-processes - Should return cognitive processes
- ✅ GET /enums/teaching-strategies - Should return teaching strategies
- ✅ GET /enums/learning-resources - Should return learning resources
- ✅ GET /enums/delivery-formats - Should return delivery formats
- ✅ GET /enums/learning-modalities - Should return learning modalities
- ✅ GET /enums/shifts - Should return shifts
- ✅ GET /enums/partial-grading-systems - Should return partial grading systems
- ✅ GET /enums/sustainable-development-goals - Should return SDGs
- ✅ GET /enums/universal-design-learning-principles - Should return UDL principles

#### RegionalTechnologicalInstituteControllerIntegrationTest (3 tests)
- ✅ GET /regional-technological-institutes - Should return all RTIs without authentication
- ✅ GET /regional-technological-institutes?userId={id} - Should return RTIs filtered by user
- ✅ GET /regional-technological-institutes - Should return empty list when no RTIs found

#### UserControllerIntegrationTest (8 tests)
- ✅ GET /users/positions - Should return current user positions when authenticated
- ✅ GET /users/positions - Should return 401 when not authenticated
- ✅ GET /users/teachers - Should return all teachers without campus filter
- ✅ GET /users/teachers?campusId=1 - Should return teachers filtered by campus
- ✅ GET /users/teachers - Should return empty list when no teachers found
- ✅ GET /users - Should return all users when no filters provided
- ✅ GET /users?role=COORDINATOR - Should return users filtered by role
- ✅ GET /users - Should return 403 when user lacks USER_READ permission

### Service Unit Tests (32 tests)

#### AuthenticationServiceTest (5 tests)
- ✅ Should login successfully with valid credentials
- ✅ Should throw exception when IP is blocked
- ✅ Should throw exception when account is blocked
- ✅ Should record failed login attempt on authentication failure
- ✅ Should throw exception when no authentication strategy found

#### CampusServiceTest (4 tests)
- ✅ Should get all campuses when userId is null
- ✅ Should get campuses by userId
- ✅ Should return empty list when no campuses found
- ✅ Should map multiple campuses correctly

#### EnumServiceTest (14 tests)
- ✅ Should get all enums
- ✅ Should get domain areas
- ✅ Should get cognitive processes
- ✅ Should get shifts
- ✅ Should get delivery formats
- ✅ Should get transversal competencies
- ✅ Should get partial grading systems
- ✅ Should get professional competencies
- ✅ Should get sustainable development goals
- ✅ Should get teaching strategies
- ✅ Should get learning modalities
- ✅ Should get learning resources
- ✅ Should get universal design learning principles
- ✅ All enum lists should have consistent structure

#### RegionalTechnologicalInstituteServiceTest (4 tests)
- ✅ Should get all RTIs when userId is null
- ✅ Should get RTIs by userId when userId is provided
- ✅ Should return empty list when no RTIs found
- ✅ Should map multiple RTIs correctly

#### UserPositionServiceTest (5 tests)
- ✅ Should get current user positions successfully
- ✅ Should throw exception when user not found
- ✅ Should get users by role and campus
- ✅ Should get all users when role and campus are null
- ✅ Should return empty list when no users found

### Utility & Helper Tests (17 tests)

#### CookieUtilTest (3 tests)
- ✅ Should add JWT cookie with encryption
- ✅ Should get and decrypt cookie value
- ✅ Should return empty when cookie not found

#### EnumUtilsTest (7 tests)
- ✅ Should not allow instantiation
- ✅ Should convert enum values to EnumResponse list
- ✅ Should find enum by name - case insensitive
- ✅ Should return null when enum name not found
- ✅ Should return null when name is null
- ✅ Should find enum by display value
- ✅ Should return null when display value not found

#### WeeklyPlanningGeneratorTest (7 tests)
- ✅ testGetMondayOfWeek()
- ✅ testGetSundayOfWeek()
- ✅ testGenerateWeeklyPlannings_SingleWeek()
- ✅ testGenerateWeeklyPlannings_ExactWeek()
- ✅ testGenerateWeeklyPlannings_CourseStartingOnMonday()
- ✅ testGenerateWeeklyPlannings_CourseStartingAndEndingMidweek()
- ✅ testGenerateWeeklyPlannings_LongCourse()

### Application Test (1 test)

#### UtecPlanificadorDocenteBackendApplicationTests (1 test)
- ✅ contextLoads() - Verifies Spring Boot application context loads successfully

## Test Execution Results

### Latest Test Run (November 26, 2025)

```
Total Tests: 89
✅ Passed: 89
❌ Failed: 0
⏭️ Skipped: 0
⏱️ Duration: ~47 seconds

Success Rate: 100%
```

### Test Distribution by Type

| Category | Tests | Percentage |
|----------|-------|------------|
| Controller Integration | 39 | 43.8% |
| Service Unit | 32 | 36.0% |
| Utility & Helper | 17 | 19.1% |
| Application | 1 | 1.1% |
| **TOTAL** | **89** | **100%** |

### Coverage by Module

| Module | Tests | Status |
|--------|-------|--------|
| Authentication | 11 | ✅ Complete |
| Campus Management | 8 | ✅ Complete |
| Course Management | 2 | ✅ Complete |
| Curricular Units | 1 | ✅ Complete |
| Enumerations | 27 | ✅ Complete |
| RTI Management | 7 | ✅ Complete |
| User Management | 13 | ✅ Complete |
| Activities | 2 | ✅ Complete |
| Utilities | 17 | ✅ Complete |
| Application Context | 1 | ✅ Complete |

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Guide](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)

---

**Last Updated**: November 26, 2025  
**Version**: 2.0  
**Status**: ✅ 89 Tests Passing (100% Success Rate)

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

**Last Updated**: November 26, 2025  
**Maintained By**: UTEC Development Team  
**Total Tests**: 89 (39 integration, 50 unit)
