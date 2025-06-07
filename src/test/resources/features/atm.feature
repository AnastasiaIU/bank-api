Feature: ATM
  Logged in users can withdraw and deposit money.

  Scenario: Cannot use ATM if not logged in
    Given a valid ATM deposit request
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 401
    And the response should contain error message "MISSING_TOKEN_OR_AUTHORIZATION_HEADER"

  Scenario: Successful deposit
    Given I am logged in with email "123@mail.com" and password "123"
    And a valid ATM deposit request
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 201
    And after processing the status should be "SUCCEEDED"

  Scenario: Successful withdrawal
    Given I am logged in with email "123@mail.com" and password "123"
    And a withdrawal request of "100.00"
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 201
    And after processing the status should be "SUCCEEDED"

  Scenario: Failed due to empty DTO
    Given I am logged in with email "123@mail.com" and password "123"
    And an empty ATM transaction payload
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 400
    And the response should contain missing fields of AtmTransactionRequestDTO

  Scenario: Failed due to invalid formats in DTO
    Given I am logged in with email "123@mail.com" and password "123"
    And an ATM transaction payload with invalid formats
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 400
    And the response should contain AtmTransactionRequestDTO fields with invalid format

  Scenario: Failed because account not found
    Given I am logged in with email "123@mail.com" and password "123"
    And a valid ATM deposit request with unknown account
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 404
    And the response should contain error message "ACCOUNT_NOT_FOUND"

  Scenario: Failed because the user is not an account owner
    Given I am logged in with email "bob@example.com" and password "1234"
    And a valid ATM deposit request
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 403
    And the response should contain error message "ACCESS_DENIED"

  Scenario: Failed due to insufficient balance
    Given I am logged in with email "123@mail.com" and password "123"
    And a withdrawal request of "11000.00"
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 201
    And after processing the status should be "FAILED"
    And after processing the failure reason should be "INSUFFICIENT_BALANCE"

  Scenario: Failed due to exceeding the daily withdrawal limit
    Given I am logged in with email "123@mail.com" and password "123"
    And a withdrawal request of "4000.00"
    When I create an ATM transaction via POST "/atm/transactions"
    Then the response status should be 201
    And after processing the status should be "FAILED"
    And after processing the failure reason should be "DAILY_WITHDRAWAL_LIMIT_EXCEEDED"