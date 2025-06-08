Feature: Account Transactions
  Employees and customers can view a list of ATM and transfer transactions for a given account.

  Scenario: Employee views all transactions for a customer account
    Given an authenticated employee
    And an account with ID 1 exists and has transactions
    When I send a GET request to "/accounts/1/transactions"
    Then the response status should be 200
    And the response should contain a list of transactions

  Scenario: Unauthorized user attempts to access account transactions
    Given a user without a valid token
    When I send a GET request to "/accounts/1/transactions"
    Then the response status should be 401

  Scenario: Customer accesses someone else's account
    Given an authenticated customer who does not own account ID 99
    When I send a GET request to "/accounts/99/transactions"
    Then the response status should be 403

  Scenario: Filtering by description returns matching transactions
    Given an authenticated employee
    And account ID 1 contains transactions with description "groceries"
    When I send a GET request to "/accounts/1/transactions?description=groceries"
    Then the response status should be 200
    And the response should only include transactions containing "groceries"