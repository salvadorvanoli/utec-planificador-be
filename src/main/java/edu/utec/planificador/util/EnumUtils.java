package edu.utec.planificador.util;

import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.enumerated.DisplayableEnum;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class EnumUtils {
    
    private EnumUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    public static <T extends Enum<T> & DisplayableEnum> List<EnumResponse> toEnumResponseList(T[] enumValues) {
        return Arrays.stream(enumValues)
                .map(enumValue -> new EnumResponse(enumValue.name(), enumValue.getDisplayValue()))
                .collect(Collectors.toList());
    }
    
    public static <T extends Enum<T> & DisplayableEnum> T findByName(Class<T> enumClass, String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static <T extends Enum<T> & DisplayableEnum> T findByDisplayValue(Class<T> enumClass, String displayValue) {
        if (displayValue == null || displayValue.trim().isEmpty()) {
            return null;
        }
        
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(enumConstant -> displayValue.equals(enumConstant.getDisplayValue()))
                .findFirst()
                .orElse(null);
    }
}
