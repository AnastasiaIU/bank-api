package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.*;
import nl.inholland.bank_api.functional.TestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class TransactionStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    @Given("a user without a valid token")
    public void aUserWithoutAValidToken() {
        context.setHeaders(new HttpHeaders());
    }

    @Given("an account with ID {int} exists and has transactions")
    public void anAccountWithIdExistsAndHasTransactions(Integer accountId) {
    }

    @Given("account ID {int} contains transactions with description {string}")
    public void accountIdContainsTransactionsWithDescription(Integer accountId, String description) {
    }

    @Given("account ID {int} contains transactions")
    public void account_id_contains_transactions(Integer accountId) {
    }

    @And("the response should contain a list of transactions")
    public void theResponseShouldContainAListOfTransactions() throws Exception {
        JsonNode body = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode content = body.get("content");

        assertThat(content).isNotNull();
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);
    }

    @And("the response should only include transactions containing {string}")
    public void theResponseShouldOnlyIncludeTransactionsWithDescription(String expectedDescription) throws Exception {
        JsonNode body = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode content = body.get("content");

        assertThat(content).isNotNull();
        for (JsonNode transaction : content) {
            String description = transaction.get("description").asText();
            assertThat(description.toLowerCase()).contains(expectedDescription.toLowerCase());
        }
    }
    @And("the response should only include transactions from {string} to {string}")
    public void theResponseShouldOnlyIncludeTransactionsInDateRange(String startDate, String endDate) throws Exception {
        JsonNode content = context.getObjectMapper().readTree(context.getResponse().getBody()).get("content");

        assertThat(content).isNotNull();
        List<String> invalidDates = new ArrayList<>();

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        for (JsonNode tx : content) {
            String timestamp = tx.get("timestamp").asText();
            LocalDate txDate = LocalDate.parse(timestamp.substring(0, 10));
            if (txDate.isBefore(start) || txDate.isAfter(end)) {
                invalidDates.add(txDate.toString());
            }
        }

        assertThat(invalidDates)
                .as("Found transactions outside the range: %s", invalidDates)
                .isEmpty();
    }
}