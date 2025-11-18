package edu.utec.planificador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BibliographicReferenceRequest {

    @NotBlank(message = "La referencia bibliográfica no puede estar vacía")
    @Size(max = 500, message = "La referencia bibliográfica no puede exceder los 500 caracteres")
    private String reference;
}
