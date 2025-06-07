package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.dto.AtmTransactionRequestDTO;
import nl.inholland.bank_api.model.enums.AtmTransactionType;
import nl.inholland.bank_api.scheduler.TransactionScheduler;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AtmStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionScheduler scheduler;

    @Autowired
    private TestContext context;

    private AtmTransactionRequestDTO request;
    private Long transactionId;

    @Given("a valid ATM deposit request")
    public void aValidAtmDepositRequest() {
        request = new AtmTransactionRequestDTO();
        request.iban = "NL91ABNA0417164300";
        request.type = AtmTransactionType.DEPOSIT;
        request.amount = new BigDecimal("50.00");
    }

    @Given("a valid ATM deposit request with unknown account")
    public void aValidAtmDepositRequestWithUnknownAccount() {
        request = new AtmTransactionRequestDTO();
        request.iban = "NL00INHO0000000000";
        request.type = AtmTransactionType.DEPOSIT;
        request.amount = new BigDecimal("50.00");
    }

    @Given("a withdrawal request of {string}")
    public void aWithdrawalRequestOf(String amount) {
        request = new AtmTransactionRequestDTO();
        request.iban = "NL91ABNA0417164300";
        request.type = AtmTransactionType.WITHDRAW;
        request.amount = new BigDecimal(amount);
    }

    @Given("an empty ATM transaction payload")
    public void anEmptyAtmTransactionPayload() {
        request = new AtmTransactionRequestDTO();
    }

    @Given("an ATM transaction payload with invalid formats")
    public void anAtmTransactionPayloadWithInvalidFormats() {
        request = new AtmTransactionRequestDTO();
        request.iban = "INVALID";
        request.amount = new BigDecimal("0");
    }

    @When("I create an ATM transaction via POST {string}")
    public void iCreateAnAtmTransactionViaPOST(String endpoint) {
        HttpEntity<AtmTransactionRequestDTO> entity = new HttpEntity<>(request, context.getHeaders());
        context.setResponse(restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class));
    }

    @And("after processing the status should be {string}")
    public void afterProcessingTheStatusShouldBe(String expected) throws Exception {
        scheduler.processAllPendingTransactions();

        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        transactionId = json.get("id").asLong();

        HttpEntity<Void> entity = new HttpEntity<>(context.getHeaders());
        ResponseEntity<String> get = restTemplate
                .exchange("/atm/transactions/" + transactionId, HttpMethod.GET, entity, String.class);

        JsonNode getJson = context.getObjectMapper().readTree(get.getBody());
        assertThat(getJson.get("status").asText()).isEqualTo(expected);
    }

    @And("after processing the failure reason should be {string}")
    public void afterProcessingTheFailureReasonShouldBe(String messageKey) throws Exception {
        String expectedFailureReason = resolveFailureReason(messageKey);

        scheduler.processAllPendingTransactions();

        HttpEntity<Void> entity = new HttpEntity<>(context.getHeaders());
        ResponseEntity<String> get = restTemplate
                .exchange("/atm/transactions/" + transactionId, HttpMethod.GET, entity, String.class);

        JsonNode getJson = context.getObjectMapper().readTree(get.getBody());
        String reason = getJson.get("failureReason").isNull() ? null : getJson.get("failureReason").asText();

        assertThat(reason).isEqualTo(expectedFailureReason);
    }

    @And("the response should contain missing fields of AtmTransactionRequestDTO")
    public void theResponseShouldContainMissingFieldsOfAtmTransactionRequestDTO() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.IBAN, ErrorMessages.IBAN_REQUIRED),
                StringUtils.fieldError(FieldNames.TYPE, ErrorMessages.TRANSACTION_TYPE_REQUIRED),
                StringUtils.fieldError(FieldNames.AMOUNT, ErrorMessages.AMOUNT_REQUIRED)
        );

        assertThat(messages).isNotNull();
        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }

    @And("the response should contain AtmTransactionRequestDTO fields with invalid format")
    public void theResponseShouldContainAtmTransactionRequestDTOFieldsWithInvalidFormat() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.IBAN, ErrorMessages.INVALID_IBAN_FORMAT),
                StringUtils.fieldError(FieldNames.AMOUNT, ErrorMessages.AMOUNT_MINIMUM)
        );

        assertThat(messages).isNotNull();
        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }

    private String resolveFailureReason(String key) {
        return switch (key) {
            case "INSUFFICIENT_BALANCE" -> ErrorMessages.INSUFFICIENT_BALANCE;
            case "DAILY_WITHDRAWAL_LIMIT_EXCEEDED" -> ErrorMessages.DAILY_WITHDRAWAL_LIMIT_EXCEEDED;
            default -> throw new IllegalArgumentException(ErrorMessages.UNKNOWN_ERROR_KEY);
        };
    }
}
