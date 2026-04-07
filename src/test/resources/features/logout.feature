Feature: Logout
  As a logged-in user
  I want to log out of the application
  So that my session is securely terminated

  Background:
    Given the user is logged in as "standard_user"

  # TC-047 — Logout via burger menu
  Scenario: User can log out via the burger menu
    When the user opens the burger menu
    And the user clicks Logout
    Then the user is on the login page

  # TC-048 — Inventory not accessible after logout
  Scenario: Inventory page is not accessible after logout
    When the user opens the burger menu
    And the user clicks Logout
    And the user navigates directly to the inventory page
    Then the user is redirected to the login page

  # TC-049 — Cart state after logout and re-login
  Scenario: Cart is empty after logout and re-login
    And the user is on the inventory page
    And the user adds "Sauce Labs Backpack" to the cart
    When the user opens the burger menu
    And the user clicks Logout
    And the user enters username "standard_user" and password "secret_sauce"
    And the user clicks the login button
    Then the cart badge is not visible
