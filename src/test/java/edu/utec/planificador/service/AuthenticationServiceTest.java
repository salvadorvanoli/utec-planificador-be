package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.LoginRequest;
import edu.utec.planificador.dto.response.AuthResponse;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.security.JwtTokenProvider;
import edu.utec.planificador.security.LoginAttemptService;
import edu.utec.planificador.service.impl.AuthenticationServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private List<AuthenticationStrategy> authenticationStrategies;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MessageService messageService;

    @Mock
    private AuthenticationStrategy mockStrategy;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "defaultAuthProvider", "LOCAL");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@utec.edu.uy");
        loginRequest.setPassword("password123");

        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getUtecEmail()).thenReturn("test@utec.edu.uy");

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        // Mock messageService responses
        when(messageService.getMessage(anyString())).thenReturn("Error message");
        when(messageService.getMessage(anyString(), any())).thenReturn("Error message with params");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_Success() {
        // Given
        String expectedToken = "jwt.token.here";
        when(loginAttemptService.isBlocked(anyString(), anyBoolean())).thenReturn(false);
        when(authenticationStrategies.stream()).thenReturn(List.of(mockStrategy).stream());
        when(mockStrategy.supports(anyString())).thenReturn(true);
        when(mockStrategy.authenticate(anyString(), anyString())).thenReturn(testUser);
        when(tokenProvider.generateTokenFromUser(testUser)).thenReturn(expectedToken);

        // When
        AuthResponse response = authenticationService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(expectedToken);

        verify(loginAttemptService, times(1)).loginSucceeded(anyString(), eq(false));
        verify(loginAttemptService, times(1)).loginSucceeded(eq(loginRequest.getEmail()), eq(true));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when IP is blocked")
    void login_BlockedIP() {
        // Given
        when(loginAttemptService.isBlocked(anyString(), eq(false))).thenReturn(true);
        when(loginAttemptService.getRemainingLockoutTime(anyString(), eq(false))).thenReturn(15L);
        when(messageService.getMessage(eq("auth.error.too-many-attempts"), any()))
            .thenReturn("Too many login attempts. Try again in 15 minutes.");

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Too many login attempts");

        verify(loginAttemptService, never()).loginSucceeded(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when account is blocked")
    void login_BlockedAccount() {
        // Given
        when(loginAttemptService.isBlocked(anyString(), eq(false))).thenReturn(false);
        when(loginAttemptService.isBlocked(eq(loginRequest.getEmail()), eq(true))).thenReturn(true);
        when(loginAttemptService.getRemainingLockoutTime(eq(loginRequest.getEmail()), eq(true))).thenReturn(30L);
        when(messageService.getMessage(eq("auth.error.account-locked"), any()))
            .thenReturn("Account locked. Try again in 30 minutes.");

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class)
            .hasMessageContaining("Account locked");

        verify(loginAttemptService, never()).loginSucceeded(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("Should record failed login attempt on authentication failure")
    void login_FailedAttempt() {
        // Given
        when(loginAttemptService.isBlocked(anyString(), anyBoolean())).thenReturn(false);
        when(authenticationStrategies.stream()).thenReturn(List.of(mockStrategy).stream());
        when(mockStrategy.supports(anyString())).thenReturn(true);
        when(mockStrategy.authenticate(anyString(), anyString()))
            .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(loginAttemptService, times(1)).loginFailed(anyString(), eq(false));
        verify(loginAttemptService, times(1)).loginFailed(eq(loginRequest.getEmail()), eq(true));
        verify(loginAttemptService, never()).loginSucceeded(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when no authentication strategy found")
    void login_NoStrategyFound() {
        // Given
        when(loginAttemptService.isBlocked(anyString(), anyBoolean())).thenReturn(false);
        when(authenticationStrategies.stream()).thenReturn(List.of(mockStrategy).stream());
        when(mockStrategy.supports(anyString())).thenReturn(false);
        when(messageSource.getMessage(eq("auth.error.no-strategy"), any(), any()))
            .thenReturn("No authentication strategy found");

        // When & Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(loginAttemptService, times(1)).loginFailed(anyString(), eq(false));
        verify(loginAttemptService, times(1)).loginFailed(eq(loginRequest.getEmail()), eq(true));
    }
}
