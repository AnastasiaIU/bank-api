package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.dto.LoginRequestDTO;
import nl.inholland.bank_api.model.dto.LoginResponseDTO;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(context.getResponse().getStatusCode().value()).isEqualTo(status);
    }

    @And("the response should contain error message {string}")
    public void theResponseShouldContainErrorMessage(String messageKey) throws Exception {
        String expectedMessage = resolveErrorMessage(messageKey);

        JsonNode json = context.getObjectMapper().readTree(context.getResponse().getBody());
        JsonNode messages = json.get("message");

        assertThat(messages).isNotNull();
        List<String> actualMessages = new ArrayList<>();
        for (JsonNode node : messages) {
            actualMessages.add(node.asText());
        }

        assertThat(actualMessages).contains(expectedMessage);
    }

    @Given("I am logged in with email {string} and password {string}")
    public void iAmLoggedInWithEmailAndPassword(String email, String password) {
        LoginRequestDTO login = new LoginRequestDTO();
        login.email = email;
        login.password = password;

        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate
                .postForEntity("/auth/login", login, LoginResponseDTO.class);

        assertThat(loginResponse.getStatusCode().is2xxSuccessful())
                .as("Login request failed with status: %s", loginResponse.getStatusCode())
                .isTrue();

        LoginResponseDTO responseBody = loginResponse.getBody();
        assertThat(responseBody).as("Login response body is null").isNotNull();
        assertThat(responseBody.token()).as("Token is null").isNotNull();

        context.setHeaders(new HttpHeaders());
        context.getHeaders().setBearerAuth(responseBody.token());
        context.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    }

    private String resolveErrorMessage(String key) {
        return switch (key) {
            case "EMAIL_EXISTS" -> StringUtils.fieldError(FieldNames.EMAIL, ErrorMessages.EMAIL_EXISTS);
            case "BSN_EXISTS" -> StringUtils.fieldError(FieldNames.BSN, ErrorMessages.BSN_EXISTS);
            case "MISSING_TOKEN_OR_AUTHORIZATION_HEADER" -> ErrorMessages.MISSING_TOKEN_OR_AUTHORIZATION_HEADER;
            case "ACCOUNT_NOT_FOUND" -> ErrorMessages.ACCOUNT_NOT_FOUND;
            case "ACCESS_DENIED" -> ErrorMessages.ACCESS_DENIED;
            default -> throw new IllegalArgumentException(ErrorMessages.UNKNOWN_ERROR_KEY);
        };
    }
}
