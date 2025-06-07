package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import nl.inholland.bank_api.functional.TestContext;
import nl.inholland.bank_api.util.JwtUtil;
import nl.inholland.bank_api.model.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrentUserStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext context;

    @Autowired
    private JwtUtil jwtUtil;

    private String jwtToken;

    @Given("I have a valid JWT token for user")
    public void i_have_a_valid_jwt_token_for_user() {
        jwtToken = jwtUtil.generateToken("123@mail.com", UserRole.CUSTOMER, 1L);;
    }

    @Given("I have an expired JWT token for user")
    public void i_have_an_expired_jwt_token_for_user() {
        jwtToken = jwtUtil.generateExpiredToken("123@mail.com", UserRole.CUSTOMER, 1L);
    }

    @When("I send a GET request to {string} with the JWT token")
    public void i_send_a_get_request_to_with_the_jwt_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @When("I send a GET request to {string} with the expired JWT token")
    public void i_send_a_get_request_to_with_expired_jwt_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @When("I send a GET request to {string} without a JWT token")
    public void i_send_a_get_request_to_without_a_jwt_token(String endpoint) {
        context.setResponse(restTemplate.getForEntity(endpoint, String.class));
    }

    @When("I send a GET request to {string} with an unsupported token")
    public void i_send_a_get_request_to_with_an_unsupported_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @Then("the response should contain the user profile with email {string}")
    public void the_response_should_contain_the_user_profile_with_email(String email) throws Exception {
        JsonNode json =  context.getObjectMapper().readTree(context.getResponse().getBody());
        assertThat(json.get("email").asText()).isEqualTo(email);
    }

    @Given("I have a JWT token with an invalid signature")
    public void i_have_a_jwt_token_with_an_invalid_signature() {
        // Generate a valid token and then tamper with the signature part
        String validToken = jwtUtil.generateToken("123@mail.com", UserRole.CUSTOMER, 1L);
        // JWT format: header.payload.signature
        // Split the token into parts
        String[] tokenParts = validToken.split("\\.");
        // Tamper with the signature by changing it (e.g., replace last char)
        String tamperedSignature = tokenParts[2].substring(0, tokenParts[2].length() - 1) +
                (tokenParts[2].endsWith("a") ? "b" : "a");
        jwtToken = tokenParts[0] + "." + tokenParts[1] + "." + tamperedSignature;
    }

    @When("I send a GET request to {string} with the invalid signature JWT token")
    public void i_send_a_get_request_to_with_the_invalid_signature_jwt_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @Given("I have a malformed JWT token")
    public void i_have_a_malformed_jwt_token() {
        // Set a token that is not even a valid JWT format (missing parts, invalid chars, etc.)
        jwtToken = "this.is.not.a.valid.jwt.token";
    }

    @When("I send a GET request to {string} with the malformed JWT token")
    public void i_send_a_get_request_to_with_the_malformed_jwt_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }
}
