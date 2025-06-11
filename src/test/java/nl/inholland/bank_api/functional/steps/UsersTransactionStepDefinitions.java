package nl.inholland.bank_api.functional.steps;

import nl.inholland.bank_api.model.dto.TransactionRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import io.cucumber.java.en.*;

import java.math.BigDecimal;
import java.util.Map;

public class UsersTransactionStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    private String authToken;
    private ResponseEntity<String> response;
    private TransactionRequestDTO transactionRequest;

    @Given("I am authenticated")
    public void i_am_authenticated_employee() {
        Map<String, String> loginPayload = Map.of(
                "email", "admin@mail.com",
                "password", "admin"
        );

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity("/auth/login", loginPayload, Map.class);

        Assertions.assertEquals(200, loginResponse.getStatusCodeValue(), "Login failed");

        authToken = "Bearer " + loginResponse.getBody().get("token").toString();
    }

    @Given("a valid transaction payload")
    public void a_valid_transaction_payload() {
        transactionRequest = new TransactionRequestDTO();
        transactionRequest.setSourceAccount("NL91ABNA0417164300");
        transactionRequest.setTargetAccount("NL91ABNA0417164301");
        transactionRequest.setInitiatedBy(1L);
        transactionRequest.setAmount(BigDecimal.valueOf(50.00));
        transactionRequest.setDescription("Monthly savings");
    }

    @When("I post transaction via POST {string}")
    public void i_post_transaction_via_post(String endpoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken.replace("Bearer ", ""));

        HttpEntity<TransactionRequestDTO> request = new HttpEntity<>(transactionRequest, headers);
        response = restTemplate.postForEntity(endpoint, request, String.class);
    }

    @Then("the response should be {int}")
    public void the_response_status_should_be(int expectedStatus) {
        Assertions.assertNotNull(response, "No response received");
        Assertions.assertEquals(expectedStatus, response.getStatusCodeValue());
    }
}