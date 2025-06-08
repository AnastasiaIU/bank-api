package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.FieldNames;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.model.dto.LoginRequestDTO;
import nl.inholland.bank_api.model.dto.LoginResponseDTO;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.*;

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
            case "EXPIRED_TOKEN" -> ErrorMessages.EXPIRED_TOKEN;
            case "MALFORMED_TOKEN" -> ErrorMessages.MALFORMED_TOKEN;
            case "INVALID_TOKEN_SIGNATURE" -> ErrorMessages.INVALID_TOKEN_SIGNATURE;
            case "UNSUPPORTED_TOKEN" -> ErrorMessages.UNSUPPORTED_TOKEN;
            default -> throw new IllegalArgumentException(ErrorMessages.UNKNOWN_ERROR_KEY);
        };
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequest(String endpoint) {
        HttpEntity<Void> request = new HttpEntity<>(context.getHeaders());
        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @Given("I am logged in as {word}")
    public void iAmLoggedInAsRole(String roleStr) {
        UserRole role = UserRole.valueOf(roleStr.toUpperCase());
        String email;
        String password;

        switch (role) {
            case EMPLOYEE -> {
                email = "admin@mail.com";
                password = "admin";
            }
            case CUSTOMER -> {
                email = "123@mail.com";
                password = "123";
            }
            default -> throw new IllegalArgumentException("Unsupported role: " + role);
        }

        iAmLoggedInWithEmailAndPassword(email, password);
    }

}
