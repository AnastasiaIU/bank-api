package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountStatus;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import nl.inholland.bank_api.model.enums.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TransactionRepositoryTest {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;


    @Test
    void sumAmountForAccountTodayReturnsCorrectTotal() {
        // Create and save a user
        User user = User.builder()
                .firstName("John").lastName("Doe")
                .email("john@test.com").password("pw")
                .bsn("123456789").phoneNumber("+1111111111")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        // Add an account for the user
        Account account = Account.builder()
                .user(user)
                .status(AccountStatus.ACTIVE)
                .iban("NL01INHO0123456789")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("100"))
                .absoluteLimit(BigDecimal.ZERO)
                .withdrawLimit(new BigDecimal("1000"))
                .dailyLimit(new BigDecimal("1000"))
                .build();
        accountRepository.save(account);

        // Succeeded transaction today, amount 100
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.SUCCEEDED)
                .amount(new BigDecimal("100"))
                .build());

        // Another succeeded transaction today, amount 50
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.SUCCEEDED)
                .amount(new BigDecimal("50"))
                .build());

        // Failed transaction today, amount 30 (should NOT be included)
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.FAILED)
                .amount(new BigDecimal("30"))
                .build());

        BigDecimal total = repository.sumAmountForAccountToday(account.getId(), LocalDate.now());
        assertThat(total).isEqualByComparingTo("150");
    }

    // Comment out @CreationTimestamp and @Column(updatable = false) annotations
    // on timestamp field for Transaction entity before running this test
   /* @Test
    void sumAmountForAccountTodayReturnsCorrectTotal() {
        // Create and save a user
        User user = User.builder()
                .firstName("John").lastName("Doe")
                .email("john@test.com").password("pw")
                .bsn("123456789").phoneNumber("+1111111111")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        // Add an account for the user
        Account account = Account.builder()
                .user(user)
                .status(AccountStatus.ACTIVE)
                .iban("NL01INHO0123456789")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("100"))
                .absoluteLimit(BigDecimal.ZERO)
                .withdrawLimit(new BigDecimal("1000"))
                .dailyLimit(new BigDecimal("1000"))
                .build();
        accountRepository.save(account);

        // Succeeded transaction today, amount 100
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.SUCCEEDED)
                .amount(new BigDecimal("100"))
                .timestamp(LocalDateTime.now())
                .build());

        // Another succeeded transaction today, amount 50
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.SUCCEEDED)
                .amount(new BigDecimal("50"))
                .timestamp(LocalDateTime.now())
                .build());

        // Failed transaction today, amount 30 (should NOT be included)
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.FAILED)
                .amount(new BigDecimal("30"))
                .timestamp(LocalDateTime.now())
                .build());

        // Succeeded transaction on another day (yesterday), amount 200 (should NOT be included)
        repository.save(Transaction.builder()
                .sourceAccount(account)
                .initiatedBy(user)
                .status(Status.SUCCEEDED)
                .amount(new BigDecimal("200"))
                .timestamp(LocalDateTime.now().minusDays(1))
                .build());

        BigDecimal total = repository.sumAmountForAccountToday(account.getId(), LocalDate.now());
        assertThat(total).isEqualByComparingTo("150");
    }
    */
}
