package edu.utec.planificador.specification;

import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.Teacher;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for building dynamic queries on Campus entity.
 * Allows filtering campuses by userId and period without creating multiple repository methods.
 */
public class CampusSpecification {

    /**
     * Creates a dynamic specification for filtering campuses based on optional parameters.
     *
     * @param userId Optional User ID to filter by (returns only campuses where the user has active positions)
     * @param period Optional period to filter by (returns only campuses that have courses in this period)
     * @return Specification that can be used with CampusRepository
     */
    public static Specification<Campus> withFilters(Long userId, String period) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by userId: campuses where the user has active positions
            if (userId != null) {
                Join<Campus, Position> positionJoin = root.join("positions", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(positionJoin.get("user").get("id"), userId));
                predicates.add(criteriaBuilder.isTrue(positionJoin.get("isActive")));
            }

            // Filter by period: campuses that have courses in this period
            if (period != null && !period.isBlank()) {
                // Join Campus -> Teacher (through many-to-many) -> Course
                Join<Campus, Teacher> teacherJoin = root.join("teachers", JoinType.INNER);
                Join<Teacher, Course> courseJoin = teacherJoin.join("courses", JoinType.INNER);
                predicates.add(criteriaBuilder.isTrue(teacherJoin.get("isActive")));
                
                // Parse period format (YYYY-1S or YYYY-2S) and filter by startDate
                try {
                    String[] parts = period.split("-");
                    if (parts.length == 2) {
                        int year = Integer.parseInt(parts[0]);
                        int semester = Integer.parseInt(parts[1].replace("S", ""));
                        
                        predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("date_part", Integer.class, 
                                criteriaBuilder.literal("year"), 
                                courseJoin.get("startDate")),
                            year
                        ));
                        
                        if (semester == 1) {
                            predicates.add(criteriaBuilder.between(
                                criteriaBuilder.function("date_part", Integer.class,
                                    criteriaBuilder.literal("month"), 
                                    courseJoin.get("startDate")),
                                1, 7
                            ));
                        } else if (semester == 2) {
                            predicates.add(criteriaBuilder.between(
                                criteriaBuilder.function("date_part", Integer.class,
                                    criteriaBuilder.literal("month"), 
                                    courseJoin.get("startDate")),
                                8, 12
                            ));
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Invalid period format, ignore the filter
                }
            }

            // Ensure distinct results and order by name
            if (query != null) {
                query.distinct(true);
                query.orderBy(criteriaBuilder.asc(root.get("name")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
