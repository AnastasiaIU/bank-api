package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    private RegisterRequestDTO request;

    @Given("a valid registration payload")
    public void aValidRegistrationPayload() {
        request = new RegisterRequestDTO();
        request.firstName = "Jane";
        request.lastName = "Doe";
        request.email = "jane.doe@example.com";
        request.password = "Password123!";
        request.bsn = "020450789";
        request.phoneNumber = "+31612345678";
    }

    @Given("a registration payload with missing required fields")
    public void aRegistrationPayloadWithMissingRequiredFields() {
        request = new RegisterRequestDTO();
    }

    @Given("a registration payload with invalid formats")
    public void aRegistrationPayloadWithInvalidFormats() {
        request = new RegisterRequestDTO();
        request.email = "invalid-email-format"; // Invalid email format
        request.bsn = "02045078"; // Invalid BSN format (should be 9 digits)
        request.phoneNumber = "123-45-00"; // Invalid phone number format
    }

    @Given("a valid registration payload with existing email")
    public void aValidRegistrationPayloadWithExistingEmail() {
        request = new RegisterRequestDTO();
        request.firstName = "Jane";
        request.lastName = "Doe";
        request.email = "123@mail.com"; // Existing email
        request.password = "Password123!";
        request.bsn = "020450789";
        request.phoneNumber = "+31612345678";
    }

    @Given("a valid registration payload with existing BSN")
    public void aValidRegistrationPayloadWithExistingBsn() {
        request = new RegisterRequestDTO();
        request.firstName = "Dane";
        request.lastName = "Doe";
        request.email = "dane.doe@example.com";
        request.password = "Password123!";
        request.bsn = "123456789";  // Existing BSN
        request.phoneNumber = "+31612345678";
    }

    @And("the response should contain a user id")
    public void theResponseShouldContainAUserId() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        assertThat(json.has("id")).isTrue();
    }

    @And("the response should contain missing fields of RegisterRequestDTO")
    public void theResponseShouldContainMissingFieldsOfRegisterRequestDTO() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.FIRST_NAME, ErrorMessages.FIRST_NAME_REQUIRED),
                StringUtils.fieldError(FieldNames.LAST_NAME, ErrorMessages.LAST_NAME_REQUIRED),
                StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_REQUIRED),
                StringUtils.fieldError(FieldNames.PASSWORD, ErrorMessages.PASSWORD_REQUIRED),
                StringUtils.fieldError(FieldNames.BSN, ErrorMessages.BSN_REQUIRED),
                StringUtils.fieldError(FieldNames.PHONE_NUMBER, ErrorMessages.PHONE_REQUIRED)
        );

        assertThat(messages).isNotNull();
        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }

    @And("the response should contain RegisterRequestDTO fields with invalid format")
    public void theResponseShouldContainRegisterRequestDTOFieldsWithInvalidFormat() throws Exception {
        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.INVALID_EMAIL_FORMAT),
                StringUtils.fieldError(FieldNames.BSN, ErrorMessages.INVALID_BSN_FORMAT),
                StringUtils.fieldError(FieldNames.PHONE_NUMBER, ErrorMessages.INVALID_PHONE_FORMAT)
        );

        assertThat(messages).isNotNull();
        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }

    @When("I register via POST {string}")
    public void iRegisterViaPOSTAuthRegister(String endpoint) {
        context.setResponse(restTemplate.postForEntity(endpoint, request, String.class));
    }
}
