package nl.inholland.bank_api.repository;

import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.AtmTransaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.*;
import nl.inholland.bank_api.model.entities.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CombinedTransactionRepositoryTest {
    @Autowired
    private EntityManager entityManager;

    private CombinedTransactionRepository combinedTransactionRepository;

    @BeforeEach
    void setUp() {
        combinedTransactionRepository = new CombinedTransactionRepository();
        combinedTransactionRepository.setEntityManager(entityManager);
        // Seed user and accounts
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("pass")
                .bsn("123456789")
                .phoneNumber("0612345678")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();
        entityManager.persist(user);

        Account source = Account.builder()
                .user(user)
                .iban("NL01BANK0000000001")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(5000))
                .absoluteLimit(BigDecimal.ZERO)
                .withdrawLimit(BigDecimal.valueOf(1000))
                .dailyLimit(BigDecimal.valueOf(2000))
                .build();
        Account target = Account.builder()
                .user(user)
                .iban("NL01BANK0000000002")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .balance(BigDecimal.valueOf(5000))
                .absoluteLimit(BigDecimal.ZERO)
                .withdrawLimit(BigDecimal.valueOf(1000))
                .dailyLimit(BigDecimal.valueOf(2000))
                .build();
        entityManager.persist(source);
        entityManager.persist(target);

        Transaction tx = Transaction.builder()
                .sourceAccount(source)
                .targetAccount(target)
                .initiatedBy(user)
                .amount(BigDecimal.valueOf(123.45))
                .status(Status.SUCCEEDED)
                .timestamp(LocalDateTime.of(2025, 1, 1, 12, 0))
                .description("Test transfer")
                .build();

        entityManager.persist(tx);


        AtmTransaction atmTx = AtmTransaction.builder()
                .account(source)
                .initiatedBy(user)
                .type(AtmTransactionType.WITHDRAW)
                .amount(BigDecimal.valueOf(50.00))
                .status(Status.SUCCEEDED)
                .timestamp(LocalDateTime.of(2025, 1, 1, 12, 1))
                .build();

        entityManager.persist(atmTx);

        entityManager.flush();
    }

    @Test
    void findAllCombined_ShouldReturnResults() {
        List<Object[]> result = combinedTransactionRepository.findAllCombined(10, 0);
        assertEquals(2, result.size());
        assertEquals("NL01BANK0000000002", result.get(1)[1]);
        assertEquals("NL01BANK0000000001", result.get(0)[0]);
    }

    @Test
    void countAllCombined_ShouldReturnCount() {
        long count = combinedTransactionRepository.countAllCombined();
        assertEquals(2, count);
    }
}