package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.aiagent.CoursePlanningDto;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.Program;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.Term;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CoursePlanningMapper {

    public CoursePlanningDto toDto(Course course) {
        if (course == null) {
            return null;
        }

        return CoursePlanningDto.builder()
                .id(course.getId())
                .shift(course.getShift().name())
                .description(course.getDescription())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .partialGradingSystem(course.getPartialGradingSystem().name())
                .hoursPerDeliveryFormat(mapDeliveryFormatHours(course.getHoursPerDeliveryFormat()))
                .isRelatedToInvestigation(course.getIsRelatedToInvestigation())
                .involvesActivitiesWithProductiveSector(course.getInvolvesActivitiesWithProductiveSector())
                .sustainableDevelopmentGoals(mapSDGs(course.getSustainableDevelopmentGoals()))
                .universalDesignLearningPrinciples(mapUDLPrinciples(course.getUniversalDesignLearningPrinciples()))
                .curricularUnit(mapCurricularUnit(course.getCurricularUnit()))
                .weeklyPlannings(mapWeeklyPlannings(course.getWeeklyPlannings()))
                .build();
    }

    private Map<String, Integer> mapDeliveryFormatHours(Map<DeliveryFormat, Integer> hours) {
        if (hours == null || hours.isEmpty()) {
            return new HashMap<>();
        }
        return hours.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        Map.Entry::getValue
                ));
    }

    private Set<String> mapSDGs(Set<SustainableDevelopmentGoal> sdgs) {
        if (sdgs == null || sdgs.isEmpty()) {
            return Set.of();
        }
        return sdgs.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private Set<String> mapUDLPrinciples(Set<UniversalDesignLearningPrinciple> principles) {
        if (principles == null || principles.isEmpty()) {
            return Set.of();
        }
        return principles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    private CoursePlanningDto.CurricularUnitDto mapCurricularUnit(CurricularUnit unit) {
        if (unit == null) {
            return null;
        }

        return CoursePlanningDto.CurricularUnitDto.builder()
                .id(unit.getId())
                .name(unit.getName())
                .credits(unit.getCredits())
                .domainAreas(unit.getDomainAreas().stream().map(Enum::name).collect(Collectors.toSet()))
                .professionalCompetencies(unit.getProfessionalCompetencies().stream().map(Enum::name).collect(Collectors.toSet()))
                .term(mapTerm(unit.getTerm()))
                .build();
    }

    private CoursePlanningDto.TermDto mapTerm(Term term) {
        if (term == null) {
            return null;
        }

        return CoursePlanningDto.TermDto.builder()
                .id(term.getId())
                .number(term.getNumber())
                .program(mapProgram(term.getProgram()))
                .build();
    }

    private CoursePlanningDto.ProgramDto mapProgram(Program program) {
        if (program == null) {
            return null;
        }

        return CoursePlanningDto.ProgramDto.builder()
                .id(program.getId())
                .name(program.getName())
                .durationInTerms(program.getDurationInTerms())
                .totalCredits(program.getTotalCredits())
                .build();
    }

    private List<CoursePlanningDto.WeeklyPlanningDto> mapWeeklyPlannings(List<WeeklyPlanning> plannings) {
        if (plannings == null || plannings.isEmpty()) {
            return List.of();
        }

        return plannings.stream()
                .map(this::mapWeeklyPlanning)
                .collect(Collectors.toList());
    }

    private CoursePlanningDto.WeeklyPlanningDto mapWeeklyPlanning(WeeklyPlanning planning) {
        if (planning == null) {
            return null;
        }

        return CoursePlanningDto.WeeklyPlanningDto.builder()
                .id(planning.getId())
                .weekNumber(planning.getWeekNumber())
                .startDate(planning.getStartDate())
                .bibliographicReferences(planning.getBibliographicReferences())
                .programmaticContents(mapProgrammaticContents(planning.getProgrammaticContents()))
                .activities(mapActivities(planning.getActivities()))
                .build();
    }

    private List<CoursePlanningDto.ProgrammaticContentDto> mapProgrammaticContents(List<ProgrammaticContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return List.of();
        }

        return contents.stream()
                .map(this::mapProgrammaticContent)
                .collect(Collectors.toList());
    }

    private CoursePlanningDto.ProgrammaticContentDto mapProgrammaticContent(ProgrammaticContent content) {
        if (content == null) {
            return null;
        }

        return CoursePlanningDto.ProgrammaticContentDto.builder()
                .id(content.getId())
                .content(content.getContent())
                .activities(mapActivities(content.getActivities()))
                .build();
    }

    private List<CoursePlanningDto.ActivityDto> mapActivities(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return List.of();
        }

        return activities.stream()
                .map(this::mapActivity)
                .collect(Collectors.toList());
    }

    private CoursePlanningDto.ActivityDto mapActivity(Activity activity) {
        if (activity == null) {
            return null;
        }

        return CoursePlanningDto.ActivityDto.builder()
                .id(activity.getId())
                .description(activity.getDescription())
                .durationInMinutes(activity.getDurationInMinutes())
                .cognitiveProcesses(mapEnumSet(activity.getCognitiveProcesses()))
                .transversalCompetencies(mapEnumSet(activity.getTransversalCompetencies()))
                .learningModality(activity.getLearningModality() != null ? activity.getLearningModality().name() : null)
                .teachingStrategies(mapEnumSet(activity.getTeachingStrategies()))
                .learningResources(mapEnumSet(activity.getLearningResources()))
                .build();
    }

    private <T extends Enum<T>> Set<String> mapEnumSet(Set<T> enumSet) {
        if (enumSet == null || enumSet.isEmpty()) {
            return Set.of();
        }
        return enumSet.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}

