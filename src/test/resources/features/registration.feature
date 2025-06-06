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
    And the response should contain missing fields

  Scenario: Invalid formats result in validation error
    Given a registration payload with invalid formats
    When I register via POST "/auth/register"
    Then the response status should be 400
    And the response should contain fields with invalid format