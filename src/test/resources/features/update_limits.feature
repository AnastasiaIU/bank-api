Feature: Update account limits

  Scenario: Update account limits successfully as EMPLOYEE
    Given I am logged in as EMPLOYEE
    When I send a PUT request to "/accounts/NL91ABNA0417164300/limits" with valid limits
    Then the response status should be 200

  Scenario: Customer cannot update account limits
    Given I am logged in as CUSTOMER
    When I send a PUT request to "/accounts/NL91ABNA0417164300/limits" with valid limits
    Then the response status should be 403

  Scenario: Update account limits with invalid values
    Given I am logged in as EMPLOYEE
    When I send a PUT request to "/accounts/NL91ABNA0417164300/limits" with invalid limits
    Then the response status should be 400
    And the response should contain validation error messages for dailyLimit and withdrawLimit

  Scenario: Update account limits for non-existing IBAN
    Given I am logged in as EMPLOYEE
    When I send a PUT request to "/accounts/NONEXISTENT_IBAN/limits" with valid limits
    Then the response status should be 404
    And the response should contain error message "ACCOUNT_NOT_FOUND"
