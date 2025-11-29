package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.mapper.PositionMapper;
import edu.utec.planificador.mapper.UserMapper;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.impl.UserPositionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserPositionService Unit Tests")
class UserPositionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PositionMapper positionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageService messageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserPositionServiceImpl userPositionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getUtecEmail()).thenReturn("john.doe@utec.edu.uy");
        when(testUser.getPositions()).thenReturn(new ArrayList<>());
        when(testUser.getPersonalData()).thenReturn(null);
        
        // Mock messageService responses
        when(messageService.getMessage(any())).thenReturn("User not found");
        when(messageService.getMessage(any(), any())).thenReturn("Error message with params");
    }

    @Test
    @DisplayName("Should get current user positions successfully")
    void getCurrentUserPositions_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userRepository.findByIdWithPositions(1L)).thenReturn(Optional.of(testUser));

        // When
        UserPositionsResponse response = userPositionService.getCurrentUserPositions();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@utec.edu.uy");

        verify(userRepository, times(1)).findByIdWithPositions(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void getCurrentUserPositions_UserNotFound() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(userRepository.findByIdWithPositions(1L)).thenReturn(Optional.empty());
        when(messageService.getMessage(any(), any())).thenReturn("User not found");

        // When & Then
        assertThatThrownBy(() -> userPositionService.getCurrentUserPositions())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    @DisplayName("Should get users by role and campus")
    void getUsers_WithRoleAndCampus() {
        // Given
        Role role = Role.TEACHER;
        Long campusId = 1L;
        List<User> users = List.of(testUser);
        UserBasicResponse basicResponse = UserBasicResponse.builder()
            .id(1L)
            .email("john.doe@utec.edu.uy")
            .build();

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);
        when(userMapper.toBasicResponse(testUser)).thenReturn(basicResponse);

        // When
        List<UserBasicResponse> responses = userPositionService.getUsers(role, campusId, null);

        // Then
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);

        verify(userRepository, times(1)).findAll(any(Specification.class));
        verify(userMapper, times(1)).toBasicResponse(testUser);
    }

    @Test
    @DisplayName("Should get all users when role and campus are null")
    void getUsers_WithoutFilters() {
        // Given
        List<User> users = List.of(testUser);
        UserBasicResponse basicResponse = UserBasicResponse.builder()
            .id(1L)
            .email("john.doe@utec.edu.uy")
            .build();

        when(userRepository.findAll(any(Specification.class))).thenReturn(users);
        when(userMapper.toBasicResponse(testUser)).thenReturn(basicResponse);

        // When
        List<UserBasicResponse> responses = userPositionService.getUsers(null, null, null);

        // Then
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);

        verify(userRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    void getUsers_EmptyList() {
        // Given
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // When
        List<UserBasicResponse> responses = userPositionService.getUsers(Role.TEACHER, 1L, null);

        // Then
        assertThat(responses).isEmpty();

        verify(userRepository, times(1)).findAll(any(Specification.class));
        verify(userMapper, never()).toBasicResponse(any());
    }
}
