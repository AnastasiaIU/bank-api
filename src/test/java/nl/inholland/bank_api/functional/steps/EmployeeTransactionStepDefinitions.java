package nl.inholland.bank_api.functional.steps;

import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.AccountStatus;
import nl.inholland.bank_api.model.enums.AccountType;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import io.cucumber.java.en.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class EmployeeTransactionStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private String authToken;
    private ResponseEntity<String> response;
    private TransactionRequestDTO transactionRequest;

    @Given("Users and accounts")
    public void users_and_accounts() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("123@mail.com")
                .password(passwordEncoder.encode("123"))
                .bsn("897374982")
                .phoneNumber("+1234567890")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.CUSTOMER)
                .build();

        User admin = User.builder()
                .firstName("Jane")
                .lastName("Foe")
                .email("admin@mail.com")
                .password(passwordEncoder.encode("admin"))
                .bsn("000234200")
                .phoneNumber("+1235550000")
                .isApproved(UserAccountStatus.APPROVED)
                .role(UserRole.EMPLOYEE)
                .build();

        userRepository.saveAll(List.of(user, admin));

        Account saving = Account.builder()
                .user(user)
                .iban("NL91ABNA0417164301")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(50000.00))
                .absoluteLimit(BigDecimal.valueOf(-100.00))
                .withdrawLimit(BigDecimal.valueOf(1000.00))
                .dailyLimit(BigDecimal.valueOf(5000.00))
                .build();

        Account checking = Account.builder()
                .user(user)
                .iban("NL91ABNA0417164300")
                .status(AccountStatus.ACTIVE)
                .type(AccountType.CHECKING)
                .balance(BigDecimal.valueOf(10000.00))
                .absoluteLimit(BigDecimal.valueOf(-300.00))
                .withdrawLimit(BigDecimal.valueOf(3000.00))
                .dailyLimit(BigDecimal.valueOf(5000.00))
                .build();

        accountRepository.saveAll(List.of(saving, checking));
    }

    @Given("I am an authenticated employee")
    public void i_am_authenticated_employee() {
        Map<String, String> loginPayload = Map.of(
                "email", "admin@mail.com",
                "password", "admin"
        );

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity("/auth/login", loginPayload, Map.class);

        Assertions.assertEquals(200, loginResponse.getStatusCodeValue(), "Login failed");

        authToken = "Bearer " + loginResponse.getBody().get("token").toString();
    }

    @Given("a valid transaction payload")
    public void a_valid_transaction_payload() {
        transactionRequest = new TransactionRequestDTO();
        transactionRequest.sourceAccount = "NL91ABNA0417164300";
        transactionRequest.targetAccount = "NL91ABNA0417164301";
        transactionRequest.initiatedBy = 1L;
        transactionRequest.amount = BigDecimal.valueOf(1.00);
        transactionRequest.description = "Monthly savings";
    }

    @When("I post transaction via POST {string}")
    public void i_post_transaction_via_post(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken.replace("Bearer ", ""));

        HttpEntity<TransactionRequestDTO> request = new HttpEntity<>(transactionRequest, headers);
        response = restTemplate.postForEntity(endpoint, request, String.class);
    }

    @Then("the response should be {int}")
    public void the_response_status_should_be(int expectedStatus) {
        Assertions.assertNotNull(response, "No response received");
        Assertions.assertEquals(expectedStatus, response.getStatusCodeValue());
    }
}