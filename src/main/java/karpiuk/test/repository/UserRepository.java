package karpiuk.test.repository;

import java.util.Optional;
import karpiuk.test.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE LOWER(u.email) = LOWER(:email) AND u.isDeleted = false")
    Optional<User> findByEmailIgnoreCaseAndFetchRoles(@Param("email") String email);
}

