package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.*;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.entities.User;
import nl.inholland.bank_api.model.enums.UserAccountStatus;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PendingUsersSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    @Autowired
    private UserRepository userRepository;

    private List<User> testUsers;

    @Given("there are customers with PENDING status and no accounts")
    public void createPendingUsers() {
        User user1 = User.builder()
                .firstName("Pending")
                .lastName("User1")
                .email("pending1@mail.com")
                .password("Password123!")
                .bsn("111111111")
                .phoneNumber("+31611111111")
                .role(UserRole.CUSTOMER)
                .isApproved(UserAccountStatus.PENDING)
                .build();

        User user2 = User.builder()
                .firstName("Pending")
                .lastName("User2")
                .email("pending2@mail.com")
                .password("Password123!")
                .bsn("222222222")
                .phoneNumber("+31622222222")
                .role(UserRole.CUSTOMER)
                .isApproved(UserAccountStatus.PENDING)
                .build();

        testUsers = userRepository.saveAll(List.of(user1, user2));
    }

    @When("I send a GET request to {string}")
    public void sendGetRequest(String endpoint) {
        HttpEntity<Void> entity = new HttpEntity<>(context.getHeaders());
        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
        context.setResponse(response);
    }

    @Then("the response should contain the pending users")
    public void theResponseShouldContainPendingUsers() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());

        assertThat(json.isArray()).isTrue();
        assertThat(json).hasSizeGreaterThanOrEqualTo(testUsers.size());

        List<String> expectedEmails = testUsers.stream().map(User::getEmail).toList();
        for (JsonNode userNode : json) {
            String email = userNode.get("email").asText();
            assertThat(expectedEmails).contains(email);
        }
    }
}