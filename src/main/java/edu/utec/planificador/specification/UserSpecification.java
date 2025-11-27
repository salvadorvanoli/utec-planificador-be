package edu.utec.planificador.specification;

import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.Role;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for building dynamic queries on User entity.
 * This allows filtering users by role, Campus, and period without creating multiple repository methods.
 */
public class UserSpecification {

    /**
     * Creates a dynamic specification for filtering users based on optional parameters.
     *
     * @param role Optional role to filter by (TEACHER, COORDINATOR, EDUCATION_MANAGER)
     * @param campusId Optional Campus ID to filter by
     * @param period Optional period to filter by (returns only users that have courses in this period)
     * @return Specification that can be used with UserRepository
     */
    public static Specification<User> withFilters(Role role, Long campusId, String period) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<User, Position> positionJoin = root.join("positions", JoinType.LEFT);

            if (role != null) {
                predicates.add(criteriaBuilder.equal(positionJoin.get("role"), role));
                predicates.add(criteriaBuilder.isTrue(positionJoin.get("isActive")));
            }

            if (campusId != null) {
                Join<Position, Campus> campusJoin = positionJoin.join("campuses", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(campusJoin.get("id"), campusId));
            }

            // Filter by period: users that have courses in this period
            // Path: User -> Teacher (Teacher extends Position) -> Courses
            if (period != null && !period.isBlank() && query != null) {
                // Use EXISTS subquery to check if user has any courses in the period
                var subquery = query.subquery(Long.class);
                var teacherRoot = subquery.from(Teacher.class);
                var courseJoin = teacherRoot.join("courses", JoinType.INNER);
                
                List<Predicate> subqueryPredicates = new ArrayList<>();
                // Teacher extends Position which has user attribute
                subqueryPredicates.add(criteriaBuilder.equal(teacherRoot.get("user").get("id"), root.get("id")));
                subqueryPredicates.add(criteriaBuilder.isTrue(teacherRoot.get("isActive")));
                
                // Parse period format (YYYY-1S or YYYY-2S) and filter by startDate
                try {
                    String[] parts = period.split("-");
                    if (parts.length == 2) {
                        int year = Integer.parseInt(parts[0]);
                        int semester = Integer.parseInt(parts[1].replace("S", ""));
                        
                        subqueryPredicates.add(criteriaBuilder.equal(
                            criteriaBuilder.function("date_part", Integer.class, 
                                criteriaBuilder.literal("year"), 
                                courseJoin.get("startDate")),
                            year
                        ));
                        
                        if (semester == 1) {
                            subqueryPredicates.add(criteriaBuilder.between(
                                criteriaBuilder.function("date_part", Integer.class,
                                    criteriaBuilder.literal("month"), 
                                    courseJoin.get("startDate")),
                                1, 7
                            ));
                        } else if (semester == 2) {
                            subqueryPredicates.add(criteriaBuilder.between(
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
                
                subquery.select(criteriaBuilder.literal(1L));
                subquery.where(criteriaBuilder.and(subqueryPredicates.toArray(new Predicate[0])));
                predicates.add(criteriaBuilder.exists(subquery));
            }

            if (query != null) {
                query.distinct(true);

                if (root.get("personalData") != null) {
                    query.orderBy(
                        criteriaBuilder.asc(root.get("personalData").get("lastName")),
                        criteriaBuilder.asc(root.get("personalData").get("name"))
                    );
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
