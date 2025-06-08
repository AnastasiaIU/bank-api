package nl.inholland.bank_api.functional.steps;

import io.cucumber.java.en.Then;
import nl.inholland.bank_api.functional.TestContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.JsonNode;
import static org.assertj.core.api.Assertions.assertThat;

public class AllAccountsStepDefintions {
    @Autowired
    private TestContext context;

    @Then("the response should contain a list of accounts")
    public void theResponseShouldContainAListOfAccounts() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        assertThat(json.get("content").isArray()).isTrue();
    }

    @Then("the response should contain at most {int} accounts")
    public void theResponseShouldContainAtMostAccounts(int maxCount) throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode accounts = json.get("content");
        assertThat(accounts.isArray()).isTrue();
        assertThat(accounts.size()).isLessThanOrEqualTo(maxCount);
    }
}
