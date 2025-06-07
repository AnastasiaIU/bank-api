package nl.inholland.bank_api.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
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
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private ResponseEntity<String> response;
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

        response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
    }

    @When("I send a GET request to {string} with the expired JWT token")
    public void i_send_a_get_request_to_with_expired_jwt_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
    }

    @When("I send a GET request to {string} without a JWT token")
    public void i_send_a_get_request_to_without_a_jwt_token(String endpoint) {
        response = restTemplate.getForEntity(endpoint, String.class);
    }

    @When("I send a GET request to {string} with a malformed JWT token")
    public void i_send_a_get_request_to_with_a_malformed_jwt_token(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.payload");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
    }

    @Then("the response should contain the user profile with email {string}")
    public void the_response_should_contain_the_user_profile_with_email(String email) throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.get("email").asText()).isEqualTo(email);
    }

    @Then("the response should contain an error message {string}")
    public void the_response_should_contain_an_error_message(String expectedMessage) throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode messages = json.get("message");
        assertThat(messages).isNotNull();

        boolean found = false;
        for (JsonNode msg : messages) {
            if (msg.asText().equals(expectedMessage)) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
    }

    @Then("the current user response status should be {int}")
    public void theLoginResponseStatusShouldBe(int status) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }
}
