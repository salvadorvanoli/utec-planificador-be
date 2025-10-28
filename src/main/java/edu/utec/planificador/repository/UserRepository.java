package edu.utec.planificador.repository;

import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUtecEmail(String utecEmail);

    boolean existsByUtecEmail(String utecEmail);

    List<User> findByAuthProvider(AuthProvider authProvider);

    List<User> findByEnabledTrue();

    @Query("SELECT u FROM User u WHERE LOWER(u.utecEmail) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> searchByEmail(@Param("email") String email);
}
