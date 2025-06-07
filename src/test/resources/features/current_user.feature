Feature: Get current authenticated user

  Scenario: Successfully get current user profile with valid JWT token
    Given I have a valid JWT token for user
    When I send a GET request to "/auth/me" with the JWT token
    Then the response status should be 200
    And the response should contain the user profile with email "123@mail.com"

  Scenario: Access /me endpoint without JWT token
    When I send a GET request to "/auth/me" without a JWT token
    Then the response status should be 401
    And the response should contain error message "MISSING_TOKEN_OR_AUTHORIZATION_HEADER"

  Scenario: Access /me endpoint with expired JWT token
    Given I have an expired JWT token for user
    When I send a GET request to "/auth/me" with the expired JWT token
    Then the response status should be 401
    And the response should contain error message "EXPIRED_TOKEN"

  Scenario: Access /me endpoint with an unsupported token
    When I send a GET request to "/auth/me" with an unsupported token
    Then the response status should be 401
    And the response should contain error message "UNSUPPORTED_TOKEN"

  Scenario: Access /me endpoint with invalid signature JWT token
    Given I have a JWT token with an invalid signature
    When I send a GET request to "/auth/me" with the invalid signature JWT token
    Then the response status should be 401
    And the response should contain error message "INVALID_TOKEN_SIGNATURE"

  Scenario: Access /me endpoint with a malformed JWT token
    Given I have a malformed JWT token
    When I send a GET request to "/auth/me" with the malformed JWT token
    Then the response status should be 401
    And the response should contain error message "MALFORMED_TOKEN"
