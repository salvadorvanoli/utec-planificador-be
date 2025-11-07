package edu.utec.planificador.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import edu.utec.planificador.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProgrammaticContentRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = Constants.MAX_PROGRAMMATIC_CONTENT_LENGTH)
    private String content;

    @Size(max = 7)
    private String color;

    @NotNull
    private Long weeklyPlanningId;
}
