package nl.inholland.bank_api.functional.steps;

import io.cucumber.java.en.*;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.dto.AccountWithUserDTO;
import nl.inholland.bank_api.model.dto.ApprovalStatusUpdateDTO;
import nl.inholland.bank_api.model.entities.Account;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.*;
import nl.inholland.bank_api.repository.AccountRepository;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApproveCustomerSignUpSteps {

    @Autowired
    private TestContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User pendingUser;

    @Given("a customer with PENDING status and no accounts")
    public void createPendingCustomer() {
        User user = User.builder()
                .firstName("Pending")
                .lastName("User")
                .email("pending.user@example.com")
                .password("Password123!")
                .bsn("123456780") // Change if necessary to avoid unique constraint errors
                .phoneNumber("+31612345678")
                .role(UserRole.CUSTOMER)
                .isApproved(UserAccountStatus.PENDING)
                .build();

        pendingUser = userRepository.save(user);
    }

    @When("I approve the customer via {string}")
    public void approveCustomer(String endpoint) {
        String url = endpoint.replace("{id}", pendingUser.getId().toString());

        ApprovalStatusUpdateDTO dto = new ApprovalStatusUpdateDTO();
        dto.setUserAccountStatus(UserAccountStatus.APPROVED);

        HttpEntity<ApprovalStatusUpdateDTO> request = new HttpEntity<>(dto, context.getHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        context.setResponse(response);
    }

    @When("I create default accounts via {string}")
    public void createAccounts(String endpoint) {
        String url = endpoint.replace("{id}", pendingUser.getId().toString());

        List<AccountWithUserDTO> accounts = List.of(
                createDTO("NL01TEST0000000001", AccountType.CHECKING),
                createDTO("NL02TEST0000000002", AccountType.SAVINGS)
        );

        HttpEntity<List<AccountWithUserDTO>> request = new HttpEntity<>(accounts, context.getHeaders());
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        context.setResponse(response);
    }

    @Then("the approved user should have status {string}")
    public void verifyUserStatus(String expectedStatus) {
        User updated = userRepository.findById(pendingUser.getId()).orElseThrow();
        assertThat(updated.getIsApproved().toString()).isEqualTo(expectedStatus);
    }

    @Then("the user should have two accounts")
    public void verifyUserHasTwoAccounts() {
        List<Account> accounts = accountRepository.findByUserId(pendingUser.getId());
        assertThat(accounts).hasSize(2);
        assertThat(accounts).allMatch(acc -> acc.getStatus() == AccountStatus.ACTIVE);
    }

    private AccountWithUserDTO createDTO(String iban, AccountType type) {
        AccountWithUserDTO dto = new AccountWithUserDTO();
        dto.setIban(iban);
        dto.setType(type.name());
        dto.setBalance(BigDecimal.ZERO);
        dto.setAbsoluteLimit(BigDecimal.ZERO);
        dto.setDailyLimit(BigDecimal.ZERO);
        dto.setWithdrawLimit(BigDecimal.ZERO);
        dto.setStatus(AccountStatus.ACTIVE.name());
        return dto;
    }
}