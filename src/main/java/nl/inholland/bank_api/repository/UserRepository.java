package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByBsn(String bsn);
    Optional<User> findByEmail(String email);
}
