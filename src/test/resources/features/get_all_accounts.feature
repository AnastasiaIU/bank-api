Feature: Get all customer accounts

  Scenario: Employee can get paginated list of accounts
    Given I am logged in as EMPLOYEE
    When I send a GET request to "/accounts"
    Then the response status should be 200
    And the response should contain a list of accounts

  Scenario: Customer cannot get list of accounts
    Given I am logged in as CUSTOMER
    When I send a GET request to "/accounts"
    Then the response status should be 403

  Scenario: Employee requests first page with 5 accounts per page
    Given I am logged in as EMPLOYEE
    When I send a GET request to "/accounts?page=0&size=5"
    Then the response status should be 200
    And the response should contain a list of accounts
    And the response should contain at most 5 accounts

