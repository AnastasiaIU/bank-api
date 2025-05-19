package nl.inholland.bank_api;

import jakarta.transaction.Transactional;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.Transaction;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.model.enums.ApprovalStatus;
import nl.inholland.bank_api.model.enums.Status;
import nl.inholland.bank_api.model.enums.ApprovalStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionRepository transactionRepository;

    public MyApplicationRunner(UserRepository userRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<User> users = userRepository.saveAll(
                List.of(
                        User.builder()
                                .firstName("Jane")
                                .lastName("Foe")
                                .email("admin@mail.com")
                                .password(passwordEncoder.encode("admin"))
                                .bsn("000000000")
                                .phoneNumber("+1235550000")
                                .isApproved(ApprovalStatus.APPROVED)
                                .role(UserRole.EMPLOYEE)
                                .build(),
                        User.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("123@mail.com")
                                .password(passwordEncoder.encode("123"))
                                .bsn("123456789")
                                .phoneNumber("+1234567890")
                                .isApproved(ApprovalStatus.APPROVED)
                                .role(UserRole.CUSTOMER)
                                .build(),
                        User.builder()
                                .firstName("Bea")
                                .lastName("Summers")
                                .email("1234@mail.com")
                                .password(passwordEncoder.encode("1234"))
                                .bsn("123456783")
                                .phoneNumber("+1234567830")
                                .isApproved(ApprovalStatus.REJECTED)
                                .role(UserRole.CUSTOMER)
                                .build()
                )
        );

        User john = users.get(1);

        Account checking = Account.builder()
                .user(john)
                .iban("NL91ABNA0417164300")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.valueOf(10000.00))
                .absoluteLimit(BigDecimal.valueOf(-300.00))
                .withdrawLimit(BigDecimal.valueOf(3000.00))
                .dailyLimit(BigDecimal.valueOf(5000.00))
                .build();

        Account savings = Account.builder()
                .user(john)
                .iban("NL91ABNA0417164301")
                .type(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(50000.00))
                .absoluteLimit(BigDecimal.valueOf(-100.00))
                .withdrawLimit(BigDecimal.valueOf(1000.00))
                .dailyLimit(BigDecimal.valueOf(5000.00))
                .build();


        accountRepository.saveAll(List.of(checking, savings));

        transactionRepository.saveAll(
                List.of(
                        Transaction.builder()
                                .amount(BigDecimal.valueOf(250.00))
                                .description("Transfer from checking to savings")
                                .sourceAccount(checking)
                                .targetAccount(savings)
                                .status(Status.SUCCEEDED)
                                .timestamp(LocalDateTime.now())
                                .build()
                )
        );
    }
}