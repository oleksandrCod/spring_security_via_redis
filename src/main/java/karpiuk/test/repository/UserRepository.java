package karpiuk.test.repository;

import java.util.Optional;
import karpiuk.test.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
