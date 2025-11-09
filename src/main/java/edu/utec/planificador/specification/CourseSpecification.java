package edu.utec.planificador.specification;

import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for building dynamic queries on Course entity.
 * This allows filtering courses by user (teacher), campus, and period without creating multiple repository methods.
 */
public class CourseSpecification {

    /**
     * Creates a dynamic specification for filtering courses based on optional parameters.
     *
     * @param userId Optional user ID to filter courses by teacher
     * @param campusId Optional campus ID to filter courses where teachers have positions in that campus
     * @param period Optional period to filter courses (format: "YYYY-1S" or "YYYY-2S")
     * @return Specification that can be used with CourseRepository
     */
    public static Specification<Course> withFilters(Long userId, Long campusId, String period) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Course, Teacher> teacherJoin = root.join("teachers", JoinType.LEFT);

            if (userId != null) {
                Join<Teacher, User> userJoin = teacherJoin.join("user", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
                predicates.add(criteriaBuilder.isTrue(teacherJoin.get("isActive")));
            }

            if (campusId != null) {
                Join<Teacher, Campus> campusJoin = teacherJoin.join("campuses", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(campusJoin.get("id"), campusId));
                if (userId == null) {
                    predicates.add(criteriaBuilder.isTrue(teacherJoin.get("isActive")));
                }
            }

            if (period != null && !period.isBlank()) {                
                try {
                    String[] parts = period.split("-");
                    if (parts.length == 2) {
                        int year = Integer.parseInt(parts[0]);
                        int semester = Integer.parseInt(parts[1].replace("S", ""));
                        
                        predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("YEAR", Integer.class, root.get("startDate")),
                            year
                        ));
                        
                        if (semester == 1) {
                            predicates.add(criteriaBuilder.equal(
                                criteriaBuilder.mod(
                                    root.get("curricularUnit").get("term").get("number"),
                                    2
                                ),
                                1
                            ));
                        } else if (semester == 2) {
                            predicates.add(criteriaBuilder.equal(
                                criteriaBuilder.mod(
                                    root.get("curricularUnit").get("term").get("number"),
                                    2
                                ),
                                0
                            ));
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Invalid period format, ignore the filter
                }
            }

            if (query != null) {
                query.distinct(true);
                
                query.orderBy(criteriaBuilder.desc(root.get("startDate")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
