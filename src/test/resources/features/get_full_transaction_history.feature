Feature: Full transaction history

  Scenario: Employee fetches combined transaction history successfully
    Given Authenticated employee
    When I fetch combined transactions with page 0 and size 10
    Then the response 200
    And the response should contain a list of all transactions

  Scenario: Non-employee tries to access combined transactions
    Given I am an authenticated customer
    When I fetch combined transactions with page 0 and size 10
    Then the response 403
    And the response body should contain "Access denied"

  Scenario: Authorized employee fetches combined transactions with no results
    Given Authenticated employee
    And the database has no transactions
    When I fetch combined transactions with page 0 and size 10
    Then the response body should be an empty content list