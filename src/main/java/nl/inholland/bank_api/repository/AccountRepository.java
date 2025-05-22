package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIban(String iban);
    List<Account> findByUserId(Long userId);
    boolean existsByIban(String iban);

    @Query("SELECT a FROM Account a WHERE a.user.firstName = :firstName AND a.user.lastName = :lastName")
    List<Account> findByFirstNameAndLastName(String firstName, String lastName);
    Page<Account> findAll(Pageable pageable);
}