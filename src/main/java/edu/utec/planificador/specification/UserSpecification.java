package edu.utec.planificador.specification;

import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
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
 * This allows filtering users by role and RTI without creating multiple repository methods.
 */
public class UserSpecification {

    /**
     * Creates a dynamic specification for filtering users based on optional parameters.
     *
     * @param role Optional role to filter by (TEACHER, COORDINATOR, EDUCATION_MANAGER)
     * @param rtiId Optional Regional Technological Institute ID to filter by
     * @return Specification that can be used with UserRepository
     */
    public static Specification<User> withFilters(Role role, Long rtiId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<User, Position> positionJoin = root.join("positions", JoinType.LEFT);

            if (role != null) {
                predicates.add(criteriaBuilder.equal(positionJoin.get("role"), role));
                predicates.add(criteriaBuilder.isTrue(positionJoin.get("isActive")));
            }

            if (rtiId != null) {
                Join<Position, Campus> campusJoin = positionJoin.join("campuses", JoinType.LEFT);
                Join<Campus, RegionalTechnologicalInstitute> rtiJoin = campusJoin.join("regionalTechnologicalInstitute", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(rtiJoin.get("id"), rtiId));
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
