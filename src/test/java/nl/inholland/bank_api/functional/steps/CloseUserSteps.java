package nl.inholland.bank_api.functional.steps;

import io.cucumber.java.en.*;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.entities.*;
import nl.inholland.bank_api.model.enums.*;
import nl.inholland.bank_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CloseUserSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User testUser;

    @Given("a registered customer with active accounts")
    public void aRegisteredCustomerWithActiveAccounts() {
        testUser = userRepository.save(User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password123!")
                .phoneNumber("+31612345678")
                .bsn("888888888")
                .role(UserRole.CUSTOMER)
                .isApproved(UserAccountStatus.APPROVED)
                .build());

        accountRepository.saveAll(List.of(
                Account.builder()
                        .user(testUser)
                        .iban("NL01TEST0123456789")
                        .type(AccountType.CHECKING)
                        .status(AccountStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(1000))
                        .dailyLimit(BigDecimal.valueOf(500))
                        .withdrawLimit(BigDecimal.valueOf(300))
                        .absoluteLimit(BigDecimal.ZERO)
                        .build(),
                Account.builder()
                        .user(testUser)
                        .iban("NL02TEST0123456789")
                        .type(AccountType.SAVINGS)
                        .status(AccountStatus.ACTIVE)
                        .balance(BigDecimal.valueOf(5000))
                        .dailyLimit(BigDecimal.valueOf(1000))
                        .withdrawLimit(BigDecimal.ZERO)
                        .absoluteLimit(BigDecimal.ZERO)
                        .build()
        ));
    }

    @When("I close the user account via PUT {string}")
    public void iCloseTheUserAccountViaPut(String endpoint) {
        String url = endpoint.replace("{id}", testUser.getId().toString());
        HttpEntity<Void> entity = new HttpEntity<>(context.getHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        context.setResponse(response);
    }

    @Then("the user should have status {string}")
    public void theUserShouldHaveStatus(String expectedStatus) {
        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getIsApproved().toString()).isEqualTo(expectedStatus);
    }

    @Then("all their accounts should have status {string}")
    public void allAccountsShouldHaveStatus(String expectedStatus) {
        List<Account> accounts = accountRepository.findByUserId(testUser.getId());
        assertThat(accounts).allMatch(acc -> acc.getStatus().toString().equals(expectedStatus));
    }
}