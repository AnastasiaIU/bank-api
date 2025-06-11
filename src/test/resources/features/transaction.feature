Feature: Transaction

  Scenario: Authenticated user a valid transaction
    Given I am authenticated
    And a valid transaction payload
    When I post transaction via POST "/transactions"
    Then the response should be 201