Feature: Login

  Scenario: Successful login
    Given a valid login payload
    When I login via POST "/auth/login"
    Then the response status should be 200
    And the login response should contain a JWT token

  Scenario: Login with missing fields
    Given a login payload with missing email and password
    When I login via POST "/auth/login"
    Then the response status should be 400
    And the login response should contain validation errors for missing fields

  Scenario: Login with invalid email and missing password
    Given a login payload with invalid email format and missing password
    When I login via POST "/auth/login"
    Then the response status should be 400
    And the login response should contain invalid email format and missing password

  Scenario: Login with invalid credentials
    Given a login payload with invalid credentials
    When I login via POST "/auth/login"
    Then the response status should be 401
    And the login response should contain bad credentials error

  Scenario: Login with a closed/rejected user account
    Given a valid login payload for a closed or rejected user
    When I login via POST "/auth/login"
    Then the response status should be 401
    And the login response should contain closed or rejected error