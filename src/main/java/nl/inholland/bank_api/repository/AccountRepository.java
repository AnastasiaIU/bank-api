package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIban(String iban);
    List<Account> findByUserId(Long userId);
}