Feature: Close a customer account
  As an employee
  I want to close a customer account
  So that the user and all their accounts are deactivated

  Scenario: Successfully close a user account
    Given I am logged in with email "admin@mail.com" and password "admin"
    And a registered customer with active accounts
    When I close the user account via PUT "/users/{id}/close"
    Then the response status should be 204
    And the user should have status "CLOSED"
    And all their accounts should have status "CLOSED"