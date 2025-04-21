package nl.inholland.bank_api;

import jakarta.transaction.Transactional;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public MyApplicationRunner(UserRepository userRepository, AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
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
                                .isApproved(true)
                                .role(UserRole.EMPLOYEE)
                                .build(),
                        User.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .email("123@mail.com")
                                .password(passwordEncoder.encode("123"))
                                .bsn("123456789")
                                .phoneNumber("+1234567890")
                                .isApproved(false)
                                .role(UserRole.CUSTOMER)
                                .build()
                )
        );

        User john = users.get(1);

        accountRepository.saveAll(
                List.of(
                        Account.builder()
                                .user(john)
                                .iban("NL91ABNA0417164300")
                                .type(AccountType.CHECKING)
                                .balance(BigDecimal.valueOf(10000.00))
                                .build()
                )
        );
    }
}