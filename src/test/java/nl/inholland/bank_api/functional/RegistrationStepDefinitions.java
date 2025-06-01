package nl.inholland.bank_api.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.inholland.bank_api.model.dto.RegisterRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class RegistrationStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequestDTO request;
    private ResponseEntity<String> response;

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

    @And("the first name is empty")
    public void theFirstNameIsEmpty() {
        request.firstName = "";
    }

    @And("the response should contain a user id")
    public void theResponseShouldContainAUserId() throws Exception {
        JsonNode json = objectMapper.readTree(response.getBody());
        assertThat(json.has("id")).isTrue();
    }

    @When("I register via POST \\/auth\\/register")
    public void iRegisterViaPOSTAuthRegister() {
        response = restTemplate.postForEntity("/auth/register", request, String.class);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }
}
