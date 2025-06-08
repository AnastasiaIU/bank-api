package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.dto.UpdateAccountLimitsDTO;
import nl.inholland.bank_api.util.StringUtils;
import nl.inholland.bank_api.constant.FieldNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import io.cucumber.java.en.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdateLimitsStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    @When("I send a PUT request to {string} with valid limits")
    public void iSendPutRequestWithValidLimits(String endpoint) throws Exception {
        UpdateAccountLimitsDTO dto = new UpdateAccountLimitsDTO();
        dto.setDailyLimit(BigDecimal.valueOf(6000));
        dto.setAbsoluteLimit(BigDecimal.valueOf(-500));
        dto.setWithdrawLimit(BigDecimal.valueOf(3500));

        String jsonRequest = context.getObjectMapper().writeValueAsString(dto);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, context.getHeaders());

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.PUT, entity, String.class);

        context.setResponse(response);
    }

    @When("I send a PUT request to {string} with invalid limits")
    public void iSendPutRequestWithInvalidLimits(String endpoint) throws Exception {
        UpdateAccountLimitsDTO dto = new UpdateAccountLimitsDTO();
        dto.setDailyLimit(BigDecimal.valueOf(-100));  // invalid
        dto.setWithdrawLimit(BigDecimal.valueOf(-100)); // invalid
        dto.setAbsoluteLimit(BigDecimal.valueOf(-500)); // allowed

        String jsonRequest = context.getObjectMapper().writeValueAsString(dto);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, context.getHeaders());

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.PUT, entity, String.class);
        context.setResponse(response);
    }

    @And("the response should contain validation error messages for dailyLimit and withdrawLimit")
    public void theResponseShouldContainValidationErrorMessagesForLimits() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.DAILY_LIMIT, ErrorMessages.DAILY_LIMIT_MINIMUM),
                StringUtils.fieldError(FieldNames.WITHDRAW_LIMIT, ErrorMessages.WITHDRAW_LIMIT_MINIMUM)
        );

        assertThat(messages).isNotNull();

        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }
}


