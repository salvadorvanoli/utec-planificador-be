package edu.utec.planificador.specification;

import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.Program;
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
            // Campus doesn't have positions, Position has campuses (many-to-many)
            if (userId != null && query != null) {
                var subquery = query.subquery(Long.class);
                var positionRoot = subquery.from(Position.class);
                var campusJoin = positionRoot.join("campuses", JoinType.INNER);
                
                subquery.select(criteriaBuilder.literal(1L));
                subquery.where(
                    criteriaBuilder.and(
                        criteriaBuilder.equal(positionRoot.get("user").get("id"), userId),
                        criteriaBuilder.isTrue(positionRoot.get("isActive")),
                        criteriaBuilder.equal(campusJoin.get("id"), root.get("id"))
                    )
                );
                
                predicates.add(criteriaBuilder.exists(subquery));
            }

            // Filter by period: campuses that have courses in this period
            // Path: Campus -> Programs -> Terms -> CurricularUnits -> Courses
            if (period != null && !period.isBlank() && query != null) {
                // Use EXISTS subquery to check if campus has any program with courses in the period
                var subquery = query.subquery(Long.class);
                var programRoot = subquery.from(Program.class);
                var termJoin = programRoot.join("terms", JoinType.INNER);
                var curricularUnitJoin = termJoin.join("curricularUnits", JoinType.INNER);
                var courseJoin = curricularUnitJoin.join("courses", JoinType.INNER);
                
                // Join Campus->Programs in the subquery to check if this program belongs to the campus
                // Campus has programs (many-to-many), Program doesn't have campuses
                var campusSubqueryRoot = subquery.from(Campus.class);
                var campusProgramsJoin = campusSubqueryRoot.join("programs", JoinType.INNER);
                
                List<Predicate> subqueryPredicates = new ArrayList<>();
                // Match: current campus ID = subquery campus ID AND subquery campus has this program
                subqueryPredicates.add(criteriaBuilder.equal(campusSubqueryRoot.get("id"), root.get("id")));
                subqueryPredicates.add(criteriaBuilder.equal(campusProgramsJoin.get("id"), programRoot.get("id")));
                
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

            // Ensure distinct results and order by name
            if (query != null) {
                query.distinct(true);
                query.orderBy(criteriaBuilder.asc(root.get("name")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
