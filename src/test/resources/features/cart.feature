Feature: Shopping Cart
  As a logged-in user
  I want to manage items in my cart
  So that I can prepare my order

  Background:
    Given the user is logged in as "standard_user"
    And the user is on the inventory page

  # TC-023 — Add single item
  Scenario: Add a single item to the cart from the inventory page
    When the user adds "Sauce Labs Backpack" to the cart
    Then the cart badge shows "1"
    And the button for "Sauce Labs Backpack" shows "Remove"

  # TC-024 — Add multiple items
  Scenario: Add multiple items to the cart
    When the user adds "Sauce Labs Backpack" to the cart
    And the user adds "Sauce Labs Bike Light" to the cart
    And the user adds "Sauce Labs Bolt T-Shirt" to the cart
    Then the cart badge shows "3"

  # TC-025 — Add from product detail
  Scenario: Add item to cart from the product detail page
    When the user clicks on the first product name
    And the user adds the item to the cart from the detail page
    Then the cart badge shows "1"

  # TC-026 — Add all items
  Scenario: Add all available items to the cart
    When the user adds all products to the cart
    Then the cart badge shows "6"
    And all product buttons show "Remove"

  # TC-027 — Cart persists after refresh
  Scenario: Cart contents persist after page refresh
    When the user adds "Sauce Labs Backpack" to the cart
    And the user adds "Sauce Labs Bike Light" to the cart
    And the user refreshes the page
    Then the cart badge shows "2"

  # TC-032 — Cart displays correct details
  Scenario: Cart page shows all added items with correct details
    When the user adds "Sauce Labs Backpack" to the cart
    And the user adds "Sauce Labs Bike Light" to the cart
    And the user navigates to the cart
    Then the cart contains "2" items
    And each cart item shows a name, description, quantity and price

  # TC-033 — Empty cart state
  Scenario: Cart page shows empty state when no items added
    When the user navigates to the cart
    Then the cart is empty

  # TC-034 — Continue shopping
  Scenario: Continue Shopping button returns to inventory
    When the user navigates to the cart
    And the user clicks "Continue Shopping"
    Then the user is back on the inventory page

  # TC-028 — Remove from inventory
  Scenario: Remove item via Remove button on the inventory page
    When the user adds "Sauce Labs Backpack" to the cart
    And the user removes "Sauce Labs Backpack" from the inventory page
    Then the cart badge is not visible
    And the button for "Sauce Labs Backpack" shows "Add to cart"

  # TC-029 — Remove from cart page
  Scenario: Remove item from the cart page
    When the user adds "Sauce Labs Backpack" to the cart
    And the user adds "Sauce Labs Bike Light" to the cart
    And the user navigates to the cart
    And the user removes "Sauce Labs Bike Light" from the cart
    Then the cart contains "1" items

  # TC-030 — Remove last item
  Scenario: Removing the last item leaves the cart empty
    When the user adds "Sauce Labs Backpack" to the cart
    And the user navigates to the cart
    And the user removes "Sauce Labs Backpack" from the cart
    Then the cart is empty
