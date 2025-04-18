package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByBsn(String bsn);
}
