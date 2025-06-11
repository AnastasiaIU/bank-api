package nl.inholland.bank_api.functional.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import nl.inholland.bank_api.repository.AtmTransactionRepository;
import nl.inholland.bank_api.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

public class FullTransactionHistoryStepDefenitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AtmTransactionRepository atmTransactionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String authToken;
    private ResponseEntity<String> response;

    @Given("Authenticated employee")
    public void authenticated_employee() {
        var loginPayload = new HashMap<String, String>();
        loginPayload.put("email", "admin@mail.com");
        loginPayload.put("password", "admin");

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity("/auth/login", loginPayload, Map.class);

        Assertions.assertEquals(200, loginResponse.getStatusCodeValue(), "Login failed");
        authToken = "Bearer " + loginResponse.getBody().get("token").toString();
    }

    @Given("I am an authenticated customer")
    public void i_am_an_authenticated_customer() {
        var loginPayload = new HashMap<String, String>();
        loginPayload.put("email", "123@mail.com");
        loginPayload.put("password", "123");

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity("/auth/login", loginPayload, Map.class);

        Assertions.assertEquals(200, loginResponse.getStatusCodeValue(), "Login failed");
        authToken = "Bearer " + loginResponse.getBody().get("token").toString();
    }

    @And("the database has no transactions")
    public void the_database_has_no_transactions() {
        atmTransactionRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @When("I fetch combined transactions with page {int} and size {int}")
    public void i_fetch_combined_transactions_with_page_and_size(int page, int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken.replace("Bearer ", ""));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String url = String.format("/combined-transactions?page=%d&size=%d", page, size);
        response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    @Then("the response {int}")
    public void the_response(int status) {
        Assertions.assertNotNull(response, "No response received");
        Assertions.assertEquals(status, response.getStatusCodeValue());
    }

    @And("the response should contain a list of all transactions")
    public void the_response_should_contain_a_list_of_alltransactions() {
        Assertions.assertNotNull(response.getBody(), "Response body is null");
        Assertions.assertTrue(response.getBody().contains("content"), "Expected 'content' in response body");
    }

    @And("the response body should contain {string}")
    public void the_response_body_should_contain(String expectedContent) {
        Assertions.assertNotNull(response.getBody(), "Response body is null");
        Assertions.assertTrue(response.getBody().contains(expectedContent),
                "Expected response body to contain: " + expectedContent);
    }

    @Then("the response body should be an empty content list")
    public void the_response_body_should_be_an_empty_content_list() throws JsonProcessingException {
        Assertions.assertNotNull(response.getBody(), "Response body is null");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        JsonNode contentNode = json.get("content");

        Assertions.assertTrue(contentNode != null && contentNode.isArray() && contentNode.isEmpty(),
                "Expected empty content list but got: " + contentNode);
    }
}