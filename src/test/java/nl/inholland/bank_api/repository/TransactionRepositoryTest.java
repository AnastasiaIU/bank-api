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
import java.util.List;

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

    @Test
    void findBySourceOrTargetAccountId_ReturnsMatchingTransactions() {
        User user = User.builder()
                .firstName("Alice").lastName("Smith")
                .email("alice@test.com").password("password")
                .bsn("987654321").phoneNumber("+2222222222")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        Account source = Account.builder()
                .user(user)
                .status(AccountStatus.ACTIVE)
                .iban("NL01INHO0000000001")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("500"))
                .absoluteLimit(BigDecimal.ZERO)
                .withdrawLimit(new BigDecimal("1000"))
                .dailyLimit(new BigDecimal("1000"))
                .build();
        accountRepository.save(source);

        Account target = Account.builder()
                .user(user)
                .status(AccountStatus.ACTIVE)
                .iban("NL01INHO0000000002")
                .type(AccountType.SAVINGS)
                .balance(new BigDecimal("300"))
                .absoluteLimit(BigDecimal.ZERO)
                .withdrawLimit(new BigDecimal("1000"))
                .dailyLimit(new BigDecimal("1000"))
                .build();
        accountRepository.save(target);

        Transaction t1 = new Transaction();
        t1.setSourceAccount(source);
        t1.setTargetAccount(target);
        t1.setInitiatedBy(user);
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setDescription("Transfer 1");
        t1.setStatus(Status.SUCCEEDED);
        t1.setTimestamp(LocalDateTime.now());
        repository.save(t1);

        Transaction t2 = new Transaction();
        t2.setSourceAccount(target);
        t2.setTargetAccount(source);
        t2.setInitiatedBy(user);
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setDescription("Transfer 2");
        t2.setStatus(Status.SUCCEEDED);
        t2.setTimestamp(LocalDateTime.now());
        repository.save(t2);

        Transaction t3 = new Transaction();
        t3.setSourceAccount(target);
        t3.setTargetAccount(target);
        t3.setInitiatedBy(user);
        t3.setAmount(BigDecimal.valueOf(300));
        t3.setDescription("Transfer 3");
        t3.setStatus(Status.SUCCEEDED);
        t3.setTimestamp(LocalDateTime.now());
        repository.save(t3);

        List<Transaction> results = repository.findBySourceAccount_IdOrTargetAccount_Id(source.getId(), source.getId());

        assertThat(results).hasSize(2);
        assertThat(results).anyMatch(t -> t.getDescription().equals("Transfer 1"));
        assertThat(results).anyMatch(t -> t.getDescription().equals("Transfer 2"));
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
