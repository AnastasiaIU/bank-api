Feature: Get current authenticated user

  Scenario: Successfully get current user profile with valid JWT token
    Given I have a valid JWT token for user
    When I send a GET request to "/auth/me" with the JWT token
    Then the current user response status should be 200
    And the response should contain the user profile with email "123@mail.com"

  Scenario: Access /me endpoint without JWT token
    When I send a GET request to "/auth/me" without a JWT token
    Then the current user response status should be 401
    And the response should contain an error message "Missing token or Authorization header"

  Scenario: Access /me endpoint with expired JWT token
    Given I have an expired JWT token for user
    When I send a GET request to "/auth/me" with the expired JWT token
    Then the current user response status should be 401
    And the response should contain an error message "Expired Token"
