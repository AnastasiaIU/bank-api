package nl.inholland.bank_api.functional.steps;

import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

public class IbanLookupStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;

    private ResponseEntity<String> response;

    private String firstName;
    private String lastName;
    private Long excludeUserId;

    @Given("Authenticated")
    public void authenticated() {
        var loginPayload = new java.util.HashMap<String, String>();
        loginPayload.put("email", "123@mail.com");
        loginPayload.put("password", "123");

        ResponseEntity<java.util.Map> loginResponse =
                restTemplate.postForEntity("/auth/login", loginPayload, java.util.Map.class);

        Assertions.assertEquals(200, loginResponse.getStatusCodeValue(), "Login failed");
        authToken = "Bearer " + loginResponse.getBody().get("token").toString();
    }

    @And("a valid name payload")
    public void a_valid_name_payload() {
        firstName = "John";
        lastName = "Doe";
        excludeUserId = 1L;
    }

    @And("a valid name payload with own id")
    public void a_valid_name_payload_with_own_id() {
        firstName = "John";
        lastName = "Doe";
        excludeUserId = 2L;
    }

    @When("I get accounts list via GET {string}")
    public void i_get_accounts_list_via_get(String endpointTemplate) {
        String endpoint = endpointTemplate
                .replace("{firstName}", firstName)
                .replace("{lastName}", lastName)
                .replace("{id}", excludeUserId.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken.replace("Bearer ", ""));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
    }

    @Then("the response is {int}")
    public void the_response_is(int expectedStatus) {
        Assertions.assertNotNull(response, "No response received");
        Assertions.assertEquals(expectedStatus, response.getStatusCodeValue());
    }

    @Then("the response body should be null")
    public void the_response_body_should_be_null() {
        Assertions.assertNotNull(response, "No response received");
        Assertions.assertTrue(
                response.getBody() == null || response.getBody().equals("[]"),
                "Expected null or empty list in response body"
        );
    }
}

