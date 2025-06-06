package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AtmTransactionRepositoryTest {
    @Autowired
    private AtmTransactionRepository repository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void sumTodayWithdrawalsByAccountReturnsCorrectTotal() {
        // Add an approved user
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

        // Withdrawal today
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(BigDecimal.TEN)
                .status(Status.SUCCEEDED)
                .build());

        // Withdrawal today but failed
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(new BigDecimal("5"))
                .status(Status.FAILED)
                .build());

        // Withdrawal today but pending
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(new BigDecimal("3"))
                .status(Status.PENDING)
                .build());

        // Deposit today
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.DEPOSIT)
                .amount(new BigDecimal("20"))
                .status(Status.SUCCEEDED)
                .build());

        BigDecimal total = repository.sumTodayWithdrawalsByAccount(account.getId(), LocalDate.now());
        assertThat(total).isEqualByComparingTo("10");
    }

    // Comment out @CreationTimestamp and @Column(updatable = false) annotations
    // on timestamp field at AtmTransaction before running this test
    /*@Test
    void sumTodayWithdrawalsReturnsCorrectTotal() {
        // Add an approved user
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

        // Withdrawal today
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(BigDecimal.TEN)
                .timestamp(LocalDateTime.now())
                .status(Status.SUCCEEDED)
                .build());

        // Withdrawal today but failed
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(new BigDecimal("5"))
                .timestamp(LocalDateTime.now())
                .status(Status.FAILED)
                .build());

        // Withdrawal today but pending
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(new BigDecimal("3"))
                .timestamp(LocalDateTime.now())
                .status(Status.PENDING)
                .build());

        // Deposit today
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.DEPOSIT)
                .amount(new BigDecimal("20"))
                .timestamp(LocalDateTime.now())
                .status(Status.SUCCEEDED)
                .build());

        // Withdrawal yesterday
        repository.save(AtmTransaction.builder()
                .account(account)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(new BigDecimal("8"))
                .timestamp(LocalDateTime.now().minusDays(1))
                .status(Status.SUCCEEDED)
                .build());

        BigDecimal total = repository.sumTodayWithdrawalsByAccount(account.getId(), LocalDate.now());
        assertThat(total).isEqualByComparingTo("10");
    }*/
}