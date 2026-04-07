Feature: Login
  As a user of SauceDemo
  I want to log in to the application
  So that I can access the product inventory

  Background:
    Given the user is on the login page

  # TC-001 — Successful login
  Scenario: Successful login with valid credentials
    When the user enters username "standard_user" and password "secret_sauce"
    And the user clicks the login button
    Then the user is redirected to the inventory page

  # TC-001 — Other valid user personas
  Scenario Outline: Login with different valid user accounts
    When the user enters username "<username>" and password "secret_sauce"
    And the user clicks the login button
    Then the user is redirected to the inventory page

    Examples:
      | username                |
      | performance_glitch_user |
      | error_user              |
      | visual_user             |
      | problem_user            |

  # TC-004 — Wrong password
  Scenario: Login fails with incorrect password
    When the user enters username "standard_user" and password "wrong_password"
    And the user clicks the login button
    Then an error message is displayed containing "Username and password do not match"

  # TC-005 — Wrong username
  Scenario: Login fails with unknown username
    When the user enters username "unknown_user" and password "secret_sauce"
    And the user clicks the login button
    Then an error message is displayed containing "Username and password do not match"

  # TC-006 — Empty username
  Scenario: Login fails when username is empty
    When the user enters username "" and password "secret_sauce"
    And the user clicks the login button
    Then an error message is displayed containing "Username is required"

  # TC-007 — Empty password
  Scenario: Login fails when password is empty
    When the user enters username "standard_user" and password ""
    And the user clicks the login button
    Then an error message is displayed containing "Password is required"

  # TC-008 — Both fields empty
  Scenario: Login fails when both fields are empty
    When the user clicks the login button
    Then an error message is displayed containing "Username is required"

  # TC-010 — SQL injection
  Scenario: Login is not vulnerable to SQL injection
    When the user enters username "' OR '1'='1" and password "anything"
    And the user clicks the login button
    Then an error message is displayed containing "Username and password do not match"

  # TC-012 — Locked out user
  Scenario: Locked out user cannot login
    When the user enters username "locked_out_user" and password "secret_sauce"
    And the user clicks the login button
    Then an error message is displayed containing "locked out"

  # TC-003 — Session not accessible after logout
  Scenario: Inventory is not accessible without login
    When the user navigates directly to the inventory page
    Then the user is redirected to the login page
