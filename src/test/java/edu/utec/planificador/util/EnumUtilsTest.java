package edu.utec.planificador.util;

import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.enumeration.Shift;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EnumUtils Unit Tests")
class EnumUtilsTest {

    @Test
    @DisplayName("Should not allow instantiation")
    void constructor_ThrowsException() {
        assertThatThrownBy(() -> {
            var constructor = EnumUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should convert enum values to EnumResponse list")
    void toEnumResponseList_Success() {
        List<EnumResponse> responses = EnumUtils.toEnumResponseList(Shift.values());

        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSizeGreaterThan(0);
        assertThat(responses.getFirst()).isNotNull();
        assertThat(responses.getFirst().getValue()).isNotNull();
        assertThat(responses.getFirst().getDisplayValue()).isNotNull();
    }

    @Test
    @DisplayName("Should find enum by name - case insensitive")
    void findByName_Success() {
        Shift shift1 = EnumUtils.findByName(Shift.class, "MORNING");
        Shift shift2 = EnumUtils.findByName(Shift.class, "morning");

        assertThat(shift1).isEqualTo(Shift.MORNING);
        assertThat(shift2).isEqualTo(Shift.MORNING);
    }

    @Test
    @DisplayName("Should return null when enum name not found")
    void findByName_NotFound() {
        Shift shift = EnumUtils.findByName(Shift.class, "INVALID_SHIFT");
        assertThat(shift).isNull();
    }

    @Test
    @DisplayName("Should return null when name is null")
    void findByName_NullName() {
        Shift shift = EnumUtils.findByName(Shift.class, null);
        assertThat(shift).isNull();
    }

    @Test
    @DisplayName("Should find enum by display value")
    void findByDisplayValue_Success() {
        Shift shift = EnumUtils.findByDisplayValue(Shift.class, Shift.MORNING.getDisplayValue());
        assertThat(shift).isEqualTo(Shift.MORNING);
    }

    @Test
    @DisplayName("Should return null when display value not found")
    void findByDisplayValue_NotFound() {
        Shift shift = EnumUtils.findByDisplayValue(Shift.class, "Invalid Display Value");
        assertThat(shift).isNull();
    }
}

