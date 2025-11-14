package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;
import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import edu.utec.planificador.mapper.RegionalTechnologicalInstituteMapper;
import edu.utec.planificador.repository.RegionalTechnologicalInstituteRepository;
import edu.utec.planificador.service.impl.RegionalTechnologicalInstituteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RegionalTechnologicalInstituteService Unit Tests")
class RegionalTechnologicalInstituteServiceTest {

    @Mock
    private RegionalTechnologicalInstituteRepository regionalTechnologicalInstituteRepository;

    @Mock
    private RegionalTechnologicalInstituteMapper regionalTechnologicalInstituteMapper;

    @InjectMocks
    private RegionalTechnologicalInstituteServiceImpl regionalTechnologicalInstituteService;

    private RegionalTechnologicalInstitute testRti;
    private RegionalTechnologicalInstituteResponse testRtiResponse;

    @BeforeEach
    void setUp() {
        // Given - Test data
        testRti = mock(RegionalTechnologicalInstitute.class);
        when(testRti.getId()).thenReturn(1L);
        when(testRti.getName()).thenReturn("Test RTI");

        testRtiResponse = RegionalTechnologicalInstituteResponse.builder()
                .id(1L)
                .name("Test RTI")
                .build();

        when(regionalTechnologicalInstituteMapper.toResponse(any(RegionalTechnologicalInstitute.class)))
                .thenReturn(testRtiResponse);
    }

    @Test
    @DisplayName("Should get all RTIs when userId is null")
    void getRegionalTechnologicalInstitutes_WithoutUserId_ReturnsAll() {
        // Given
        List<RegionalTechnologicalInstitute> rtis = List.of(testRti);
        when(regionalTechnologicalInstituteRepository.findAll()).thenReturn(rtis);

        // When
        List<RegionalTechnologicalInstituteResponse> result =
                regionalTechnologicalInstituteService.getRegionalTechnologicalInstitutes(null);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test RTI");
        verify(regionalTechnologicalInstituteRepository, times(1)).findAll();
        verify(regionalTechnologicalInstituteRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("Should get RTIs by userId when userId is provided")
    void getRegionalTechnologicalInstitutes_WithUserId_ReturnsFiltered() {
        // Given
        Long userId = 1L;
        List<RegionalTechnologicalInstitute> rtis = List.of(testRti);
        when(regionalTechnologicalInstituteRepository.findByUserId(userId)).thenReturn(rtis);

        // When
        List<RegionalTechnologicalInstituteResponse> result =
                regionalTechnologicalInstituteService.getRegionalTechnologicalInstitutes(userId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(regionalTechnologicalInstituteRepository, times(1)).findByUserId(userId);
        verify(regionalTechnologicalInstituteRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no RTIs found")
    void getRegionalTechnologicalInstitutes_NoRtis_ReturnsEmptyList() {
        // Given
        when(regionalTechnologicalInstituteRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<RegionalTechnologicalInstituteResponse> result =
                regionalTechnologicalInstituteService.getRegionalTechnologicalInstitutes(null);

        // Then
        assertThat(result).isEmpty();
        verify(regionalTechnologicalInstituteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should map multiple RTIs correctly")
    void getRegionalTechnologicalInstitutes_MultipleRtis_MapsAll() {
        // Given
        RegionalTechnologicalInstitute rti2 = mock(RegionalTechnologicalInstitute.class);
        when(rti2.getId()).thenReturn(2L);
        when(rti2.getName()).thenReturn("Test RTI 2");

        RegionalTechnologicalInstituteResponse response2 = RegionalTechnologicalInstituteResponse.builder()
                .id(2L)
                .name("Test RTI 2")
                .build();

        List<RegionalTechnologicalInstitute> rtis = List.of(testRti, rti2);
        when(regionalTechnologicalInstituteRepository.findAll()).thenReturn(rtis);
        when(regionalTechnologicalInstituteMapper.toResponse(testRti)).thenReturn(testRtiResponse);
        when(regionalTechnologicalInstituteMapper.toResponse(rti2)).thenReturn(response2);

        // When
        List<RegionalTechnologicalInstituteResponse> result =
                regionalTechnologicalInstituteService.getRegionalTechnologicalInstitutes(null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RegionalTechnologicalInstituteResponse::getName)
                .containsExactly("Test RTI", "Test RTI 2");
        verify(regionalTechnologicalInstituteMapper, times(2)).toResponse(any());
    }
}

