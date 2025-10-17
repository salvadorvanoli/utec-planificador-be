package edu.utec.planificador.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnumResponse {
    
    private String value;
    private String displayValue;
}
