package nl.inholland.bank_api.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.model.dto.LoginRequestDTO;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO request;
    private ResponseEntity<String> response;

    @Given("a valid login payload")
    public void aValidLoginPayload() {
        request = new LoginRequestDTO();
        request.email = "123@mail.com";
        request.password = "123";
    }

    @Given("a login payload with missing email and password")
    public void aLoginPayloadWithMissingFields() {
        request = new LoginRequestDTO(); // Both fields null
    }

    @Given("a login payload with invalid email format and missing password")
    public void aLoginPayloadWithInvalidEmailAndMissingPassword() {
        request = new LoginRequestDTO();
        request.email = "invalid-email";
    }

    @Given("a login payload with invalid credentials")
    public void aLoginPayloadWithInvalidCredentials() {
        request = new LoginRequestDTO();
        request.email = "wrong@example.com";
        request.password = "wrongPassword!";
    }

    @When("I login via POST {string}")
    public void iLoginViaPOST(String endpoint) {
        response = restTemplate.postForEntity(endpoint, request, String.class);
    }

    @Then("the login response status should be {int}")
    public void theLoginResponseStatusShouldBe(int status) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }

    @And("the login response should contain a JWT token")
    public void theLoginResponseShouldContainAJWTToken() throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.has("token")).isTrue();
    }

    @And("the login response should contain validation errors for missing fields")
    public void theLoginResponseShouldContainValidationErrorsForMissingFields() throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_REQUIRED),
                StringUtils.fieldError(FieldNames.PASSWORD, ErrorMessages.PASSWORD_REQUIRED)
        );

        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }

    @And("the login response should contain invalid email format and missing password")
    public void theLoginResponseShouldContainInvalidEmailFormatAndMissingPassword() throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode messages = json.get("message");

        List<String> expectedMessages = List.of(
                StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.INVALID_EMAIL_FORMAT),
                StringUtils.fieldError(FieldNames.PASSWORD, ErrorMessages.PASSWORD_REQUIRED)
        );

        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).containsAll(expectedMessages);
    }

    @And("the login response should contain bad credentials error")
    public void theLoginResponseShouldContainBadCredentialsError() throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode messages = json.get("message");

        assertThat(messages).isNotNull();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).asText()).isEqualTo(ErrorMessages.INVALID_EMAIL_OR_PASSWORD);
    }
}
