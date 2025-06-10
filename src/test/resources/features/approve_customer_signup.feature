Feature: Approve customer signup and create accounts
  As an employee
  I want to approve a customer
  So that their accounts are created automatically

  Scenario: Approve a pending customer and create accounts
    Given I am logged in as EMPLOYEE
    And a customer with PENDING status and no accounts
    When I approve the customer via "/users/{id}/approval-status"
    And I create default accounts via "/users/{id}/accounts"
    Then the response status should be 201
    Then the approved user should have status "APPROVED"
    And the user should have two accounts