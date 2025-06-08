package nl.inholland.bank_api.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.assertj.core.api.Assertions.*;

public class TransactionStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private ResponseEntity<String> response;
    private HttpHeaders headers = new HttpHeaders();

    @Given("an authenticated employee")
    public void anAuthenticatedEmployee() {
        // Replace with a valid JWT if needed
        headers.setBearerAuth("mocked.employee.jwt.token");
    }

    @Given("an account with ID {int} exists and has transactions")
    public void anAccountWithIDExistsAndHasTransactions(int accountId) {
        // This can be stubbed or assumed to exist in the test DB
    }

    @Given("an authenticated customer who does not own account ID {int}")
    public void anUnauthorizedCustomer(int accountId) {
        headers.setBearerAuth("mocked.customer.jwt.token"); // a customer token that doesn't own the account
    }

    @Given("a user without a valid token")
    public void aUserWithoutAValidToken() {
        headers.remove("Authorization");
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String endpoint) {
        HttpEntity<Void> request = new HttpEntity<>(headers);
        response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @And("the response should contain a list of transactions")
    public void theResponseShouldContainAListOfTransactions() throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode content = json.get("content");

        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);
    }

    @And("the response should only include transactions containing {string}")
    public void theResponseShouldOnlyIncludeTransactionsWithDescription(String expectedDescription) throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode content = json.get("content");

        assertThat(content).isNotNull();
        for (JsonNode transaction : content) {
            String description = transaction.get("description").asText();
            assertThat(description.toLowerCase()).contains(expectedDescription.toLowerCase());
        }
    }
}