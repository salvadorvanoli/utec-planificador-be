package edu.utec.planificador.specification;

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
     * @param campusId Optional campus ID to filter courses directly by their campus relationship
     * @param period Optional period to filter courses (format: "YYYY-1S" or "YYYY-2S")
     * @param searchText Optional text to search in curricular unit name or program name
     * @return Specification that can be used with CourseRepository
     */
    public static Specification<Course> withFilters(Long userId, Long campusId, String period, String searchText) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by campus directly through Course -> Campus relationship
            if (campusId != null) {
                predicates.add(criteriaBuilder.equal(root.get("campus").get("id"), campusId));
            }

            // Filter by user (teacher) - only join teachers if needed
            if (userId != null) {
                Join<Course, Teacher> teacherJoin = root.join("teachers", JoinType.INNER);
                Join<Teacher, User> userJoin = teacherJoin.join("user", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(userJoin.get("id"), userId));
                predicates.add(criteriaBuilder.isTrue(teacherJoin.get("isActive")));
            }

            if (period != null && !period.isBlank()) {                
                try {
                    String[] parts = period.split("-");
                    if (parts.length == 2) {
                        int year = Integer.parseInt(parts[0]);
                        int semester = Integer.parseInt(parts[1].replace("S", ""));
                        
                        predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("date_part", Integer.class, 
                                criteriaBuilder.literal("year"), 
                                root.get("startDate")),
                            year
                        ));
                        
                        if (semester == 1) {
                            predicates.add(criteriaBuilder.between(
                                criteriaBuilder.function("date_part", Integer.class,
                                    criteriaBuilder.literal("month"), 
                                    root.get("startDate")),
                                1, 7
                            ));
                        } else if (semester == 2) {
                            predicates.add(criteriaBuilder.between(
                                criteriaBuilder.function("date_part", Integer.class,
                                    criteriaBuilder.literal("month"), 
                                    root.get("startDate")),
                                8, 12
                            ));
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Invalid period format, ignore the filter
                }
            }

            if (searchText != null && !searchText.isBlank()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                
                Predicate curricularUnitNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("curricularUnit").get("name")),
                    searchPattern
                );
                
                Predicate programNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("curricularUnit").get("term").get("program").get("name")),
                    searchPattern
                );
                
                predicates.add(criteriaBuilder.or(curricularUnitNamePredicate, programNamePredicate));
            }

            if (query != null) {
                query.distinct(true);
                
                query.orderBy(criteriaBuilder.desc(root.get("startDate")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
