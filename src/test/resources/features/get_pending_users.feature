Feature: View pending users
  As an employee
  I want to view customers without accounts
  So that I can review and approve them

  Scenario: Employee retrieves list of pending users
    Given I am logged in as EMPLOYEE
    And there are customers with PENDING status and no accounts
    When I request the list of pending users from "/users/pending"
    Then the response status should be 200
    And the response should contain the pending users