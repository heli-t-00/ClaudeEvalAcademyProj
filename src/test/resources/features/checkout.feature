Feature: Checkout
  As a logged-in user with items in my cart
  I want to complete the checkout process
  So that I can place my order

  Background:
    Given the user is logged in as "standard_user"
    And the user has "Sauce Labs Backpack" in the cart
    And the user navigates to the cart
    And the user clicks Checkout

  # TC-037 — Valid checkout info
  Scenario: Complete checkout with valid information
    When the user enters checkout info: first name "John", last name "Doe", zip "12345"
    And the user clicks Continue
    Then the order overview page is displayed
    And the item "Sauce Labs Backpack" is listed in the overview
    And the total price is correct

  # TC-043 — Complete purchase
  Scenario: Complete purchase via Finish button
    When the user enters checkout info: first name "John", last name "Doe", zip "12345"
    And the user clicks Continue
    And the user clicks Finish
    Then the order confirmation page is displayed
    And the confirmation message contains "Thank you for your order"

  # TC-044 — Cart cleared after purchase
  Scenario: Cart is empty after successful purchase
    When the user enters checkout info: first name "John", last name "Doe", zip "12345"
    And the user clicks Continue
    And the user clicks Finish
    And the user clicks Back Home
    Then the cart badge is not visible

  # TC-038 — Empty first name
  Scenario: Checkout fails when first name is missing
    When the user enters checkout info: first name "", last name "Doe", zip "12345"
    And the user clicks Continue
    Then a checkout error is displayed containing "First Name is required"

  # TC-039 — Empty last name
  Scenario: Checkout fails when last name is missing
    When the user enters checkout info: first name "John", last name "", zip "12345"
    And the user clicks Continue
    Then a checkout error is displayed containing "Last Name is required"

  # TC-040 — Empty zip
  Scenario: Checkout fails when postal code is missing
    When the user enters checkout info: first name "John", last name "Doe", zip ""
    And the user clicks Continue
    Then a checkout error is displayed containing "Postal Code is required"

  # TC-041 — Cancel returns to cart
  Scenario: Cancel on checkout step 1 returns to cart
    When the user clicks Cancel on the checkout info page
    Then the user is on the cart page
    And the cart contains "1" items

  # TC-045 — Cancel on overview returns to cart
  Scenario: Cancel on order overview returns to cart with items intact
    When the user enters checkout info: first name "John", last name "Doe", zip "12345"
    And the user clicks Continue
    And the user clicks Cancel on the overview page
    Then the user is on the cart page
    And the cart contains "1" items

  # TC-042 — Overview totals correct
  Scenario: Order overview shows correct subtotal and tax
    When the user enters checkout info: first name "Jane", last name "Smith", zip "90210"
    And the user clicks Continue
    Then the order overview page is displayed
    And the subtotal matches the sum of item prices
    And the order total equals subtotal plus tax
