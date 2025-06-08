package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.databind.JsonNode;
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

    @Given("I have an expired JWT token for user")
    public void iHaveAnExpiredJwtTokenForUser() {
        jwtToken = jwtUtil.generateExpiredToken("123@mail.com", UserRole.CUSTOMER, 1L);
    }

    @When("I send a GET request to {string} with an expired JWT token")
    public void iSendAGetRequestToWithExpiredJwtToken(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @When("I send a GET request to {string} without a JWT token")
    public void iSendAGetRequestToWithoutAJwtToken(String endpoint) {
        context.setResponse(restTemplate.getForEntity(endpoint, String.class));
    }

    @When("I send a GET request to {string} with an unsupported token")
    public void iSendAGetRequestToWithAnUnsupportedToken(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @Then("the response should contain the user profile with email {string}")
    public void theResponseShouldContainTheUserProfileWithEmail(String email) throws Exception {
        JsonNode json =  context.getObjectMapper().readTree(context.getResponse().getBody());
        assertThat(json.get("email").asText()).isEqualTo(email);
    }

    @Given("I have a JWT token with an invalid signature")
    public void iHaveAJwtTokenWithAnInvalidSignature() {
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

    @When("I send a GET request to {string} with an invalid signature JWT token")
    public void iSendAGetRequestToWithTheInvalidSignatureJwtToken(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }

    @Given("I have a malformed JWT token")
    public void  iHaveAMalformedJwtToken() {
        // Set a token that is not even a valid JWT format (missing parts, invalid chars, etc.)
        jwtToken = "this.is.not.a.valid.jwt.token";
    }

    @When("I send a GET request to {string} with a malformed JWT token")
    public void iSendAGetRequestToWithTheMalformedJwtToken(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        context.setResponse(response);
    }
}
