Feature: Registration
  Users can create an account by providing valid personal information.

  Scenario: Successful registration
    Given a valid registration payload
    When I register via POST "/auth/register"
    Then the response status should be 201
    And the response should contain a user id

  Scenario: Missing first name results in validation error
    Given a valid registration payload
    And the first name is empty
    When I register via POST "/auth/register"
    Then the response status should be 400