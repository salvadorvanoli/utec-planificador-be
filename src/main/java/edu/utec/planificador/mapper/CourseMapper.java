package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.CourseBasicResponse;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.Modification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final UserMapper userMapper;

    /**
     * Maps a Course entity to a CourseResponse DTO.
     * This includes all course information for detailed views.
     */
    public CourseResponse toResponse(Course course) {
        if (course == null) {
            return null;
        }

        return CourseResponse.builder()
            .id(course.getId())
            .shift(course.getShift())
            .description(course.getDescription())
            .startDate(course.getStartDate())
            .endDate(course.getEndDate())
            .partialGradingSystem(course.getPartialGradingSystem())
            .hoursPerDeliveryFormat(course.getHoursPerDeliveryFormat())
            .isRelatedToInvestigation(course.getIsRelatedToInvestigation())
            .involvesActivitiesWithProductiveSector(course.getInvolvesActivitiesWithProductiveSector())
            .sustainableDevelopmentGoals(course.getSustainableDevelopmentGoals())
            .universalDesignLearningPrinciples(course.getUniversalDesignLearningPrinciples())
            .curricularUnitId(course.getCurricularUnit().getId())
            .build();
    }

    /**
     * Maps a Course entity to a CourseBasicResponse DTO.
     * This includes only the essential information for course listing:
     * - Course ID
     * - Curricular unit name
     * - List of teachers (as UserBasicResponse)
     * - Last modification date (from most recent Modification)
     */
    public CourseBasicResponse toBasicResponse(Course course) {
        if (course == null) {
            return null;
        }

        // Map teachers to UserBasicResponse using UserMapper
        List<UserBasicResponse> teachers = course.getTeachers().stream()
            .map(teacher -> userMapper.toBasicResponse(teacher.getUser()))
            .toList();

        // Get the last modification date
        LocalDateTime lastModificationDate = course.getModifications().stream()
            .map(Modification::getModificationDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        return CourseBasicResponse.builder()
            .id(course.getId())
            .description(course.getDescription())
            .startDate(course.getStartDate())
            .endDate(course.getEndDate())
            .curricularUnitName(course.getCurricularUnit().getName())
            .teachers(teachers)
            .lastModificationDate(lastModificationDate)
            .build();
    }
}
