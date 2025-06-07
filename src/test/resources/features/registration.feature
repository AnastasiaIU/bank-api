Feature: Registration
  Users can create an account by providing valid personal information.

  Scenario: Successful registration
    Given a valid registration payload
    When I register via POST "/auth/register"
    Then the response status should be 201
    And the response should contain a user id

  Scenario: Missing required fields result in validation error
    Given a registration payload with missing required fields
    When I register via POST "/auth/register"
    Then the response status should be 400
    And the response should contain missing fields of RegisterRequestDTO

  Scenario: Invalid formats result in validation error
    Given a registration payload with invalid formats
    When I register via POST "/auth/register"
    Then the response status should be 400
    And the response should contain RegisterRequestDTO fields with invalid format

  Scenario: Failed to register because email already exists
    Given a valid registration payload with existing email
    When I register via POST "/auth/register"
    Then the response status should be 409
    And the response should contain error message "EMAIL_EXISTS"

  Scenario: Failed to register because BSN already exists
    Given a valid registration payload with existing BSN
    When I register via POST "/auth/register"
    Then the response status should be 409
    And the response should contain error message "BSN_EXISTS"