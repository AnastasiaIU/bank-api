Feature: Iban Lookup

  Scenario: Authenticated user a list of accounts found by name
    Given Authenticated
    And a valid name payload
    When I get accounts list via GET "/users/accounts/{firstName}/{lastName}/{id}"
    Then the response is 200

  Scenario: Authenticated user finds no accounts with matching name when own ID is excluded
    Given Authenticated
    And a valid name payload with own id
    When I get accounts list via GET "/users/accounts/{firstName}/{lastName}/{id}"
    Then the response body should be null