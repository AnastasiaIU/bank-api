package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByBsn(String bsn);
    List<User> findByIsApproved(ApprovalStatus approvalStatus);
    User findById(long id);
    Optional<User> findByEmail(String email);
}
