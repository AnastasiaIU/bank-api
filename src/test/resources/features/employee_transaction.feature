Feature: Transaction

  Scenario: Authenticated employee posts a valid transaction
    Given Users and accounts
    Given I am an authenticated employee
    And a valid transaction payload
    When I post transaction via POST "/transactions"
    Then the response should be 201