package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.CampusResponse;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.mapper.CampusMapper;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.service.impl.CampusServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    private Campus testCampus;
    private CampusResponse campusResponse;

    @BeforeEach
    void setUp() {
        testCampus = mock(Campus.class);
        when(testCampus.getId()).thenReturn(1L);
        when(testCampus.getName()).thenReturn("Campus Test");

        campusResponse = CampusResponse.builder()
            .id(1L)
            .name("Campus Test")
            .build();
    }

    @Test
    @DisplayName("Should get all campuses when userId is null")
    void getCampuses_WithoutUserId() {
        // Given
        List<Campus> campuses = List.of(testCampus);
        when(campusRepository.findAll()).thenReturn(campuses);
        when(campusMapper.toResponse(testCampus)).thenReturn(campusResponse);

        // When
        List<CampusResponse> responses = campusService.getCampuses(null);

        // Then
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getName()).isEqualTo("Campus Test");

        verify(campusRepository, times(1)).findAll();
        verify(campusRepository, never()).findByUserId(anyLong());
        verify(campusMapper, times(1)).toResponse(testCampus);
    }

    @Test
    @DisplayName("Should get campuses by userId")
    void getCampuses_WithUserId() {
        // Given
        Long userId = 1L;
        List<Campus> campuses = List.of(testCampus);
        when(campusRepository.findByUserId(userId)).thenReturn(campuses);
        when(campusMapper.toResponse(testCampus)).thenReturn(campusResponse);

        // When
        List<CampusResponse> responses = campusService.getCampuses(userId);

        // Then
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);

        verify(campusRepository, times(1)).findByUserId(userId);
        verify(campusRepository, never()).findAll();
        verify(campusMapper, times(1)).toResponse(testCampus);
    }

    @Test
    @DisplayName("Should return empty list when no campuses found")
    void getCampuses_EmptyList() {
        // Given
        when(campusRepository.findAll()).thenReturn(List.of());

        // When
        List<CampusResponse> responses = campusService.getCampuses(null);

        // Then
        assertThat(responses).isEmpty();

        verify(campusRepository, times(1)).findAll();
        verify(campusMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should map multiple campuses correctly")
    void getCampuses_MultipleCampuses() {
        // Given
        Campus campus2 = mock(Campus.class);
        when(campus2.getId()).thenReturn(2L);
        when(campus2.getName()).thenReturn("Campus 2");

        CampusResponse response2 = CampusResponse.builder()
            .id(2L)
            .name("Campus 2")
            .build();

        List<Campus> campuses = List.of(testCampus, campus2);
        when(campusRepository.findAll()).thenReturn(campuses);
        when(campusMapper.toResponse(testCampus)).thenReturn(campusResponse);
        when(campusMapper.toResponse(campus2)).thenReturn(response2);

        // When
        List<CampusResponse> responses = campusService.getCampuses(null);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Campus Test");
        assertThat(responses.get(1).getName()).isEqualTo("Campus 2");

        verify(campusMapper, times(2)).toResponse(any());
    }
}
