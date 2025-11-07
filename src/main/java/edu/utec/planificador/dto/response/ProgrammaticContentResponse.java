package edu.utec.planificador.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ProgrammaticContentResponse {
    private Long id;
    private String title;
    private String content;
    private String color;
    private Long weeklyPlanningId;
    private List<Long> activityIds;
}
