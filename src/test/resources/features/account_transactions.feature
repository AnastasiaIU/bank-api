Feature: Account Transactions
  Employees and customers can view a list of ATM and transfer transactions for a given account.

  Scenario: Employee views all transactions for a customer account
    Given I am logged in with email "admin@mail.com" and password "admin"
    And an account with ID 1 exists and has transactions
    When I send a GET request to "/accounts/1/transactions"
    Then the response status should be 200
    And the response should contain a list of transactions

  Scenario: Unauthorized user attempts to access account transactions
    Given a user without a valid token
    When I send a GET request to "/accounts/1/transactions"
    Then the response status should be 401

  Scenario: Customer accesses someone else's account
    Given I am logged in with email "bob@example.com" and password "1234"
    When I send a GET request to "/accounts/99/transactions"
    Then the response status should be 403

  Scenario: Filtering by description returns matching transactions
    Given I am logged in with email "123@mail.com" and password "123"
    And account ID 1 contains transactions with description "transfer"
    When I send a GET request to "/accounts/1/transactions?description=transfer"
    Then the response status should be 200
    And the response should only include transactions containing "transfer"

  Scenario: Filter transactions by date range
    Given I am logged in with email "123@mail.com" and password "123"
    And account ID 1 contains transactions
    When I send a GET request to "/accounts/1/transactions?startDate=2024-01-01&endDate=2024-01-31"
    Then the response status should be 200
    And the response should only include transactions from "2024-01-01" to "2024-01-31"

