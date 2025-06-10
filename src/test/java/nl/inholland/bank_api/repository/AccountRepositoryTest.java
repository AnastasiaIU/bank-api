package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountStatus;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AccountRepositoryTest {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByIban_Exists() {
        // Arrange
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .bsn("123456789")
                .phoneNumber("0612345678")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        user = userRepository.save(user);

        Account account = Account.builder()
                .iban("NL01BANK0000000001")
                .balance(BigDecimal.valueOf(1000))
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .user(user)
                .absoluteLimit(new BigDecimal("5000.00"))
                .dailyLimit(new BigDecimal("500.00"))
                .withdrawLimit(new BigDecimal("300.00"))
                .build();

        accountRepository.save(account);

        // Act
        Optional<Account> result = accountRepository.findByIban("NL01BANK0000000001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("NL01BANK0000000001", result.get().getIban());
        assertEquals(BigDecimal.valueOf(1000), result.get().getBalance());
    }

    @Test
    void testFindByFirstNameAndLastName_ExcludingId() {
        // Arrange
        User user1 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john1@example.com")
                .password("password")
                .bsn("123456789")
                .phoneNumber("0612345678")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        User user2 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john2@example.com")
                .password("password")
                .bsn("987654321")
                .phoneNumber("0698765432")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        Account account1 = Account.builder()
                .user(user1)
                .status(AccountStatus.ACTIVE)
                .iban("NL01BANK0000000001")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .absoluteLimit(new BigDecimal("-500.00"))
                .withdrawLimit(new BigDecimal("500.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .build();

        Account account2 = Account.builder()
                .user(user2)
                .status(AccountStatus.ACTIVE)
                .iban("NL01BANK0000000002")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("2000.00"))
                .absoluteLimit(new BigDecimal("-500.00"))
                .withdrawLimit(new BigDecimal("500.00"))
                .dailyLimit(new BigDecimal("1000.00"))
                .build();

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Act
        List<Account> result = accountRepository.findByFirstNameAndLastName("John", "Doe", user1.getId());

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user2.getId());
    }
}