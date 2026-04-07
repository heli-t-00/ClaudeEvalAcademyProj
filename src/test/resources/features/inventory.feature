Feature: Inventory / Product Listings
  As a logged-in user
  I want to browse and sort the product catalogue
  So that I can find items to purchase

  Background:
    Given the user is logged in as "standard_user"
    And the user is on the inventory page

  # TC-014 — Product cards present
  Scenario: Inventory page displays product cards with all required fields
    Then all product cards display a name, description, price and add to cart button

  # TC-015 — Images load
  Scenario: Product images load without broken links
    Then no product images are broken

  # TC-016 — Navigate to product detail
  Scenario: Clicking a product name navigates to the product detail page
    When the user clicks on the first product name
    Then the product detail page is displayed
    And the product detail page shows a name, description, price and add to cart button

  # TC-018 — Sort A to Z
  Scenario: Sort products alphabetically A to Z
    When the user sorts products by "Name (A to Z)"
    Then the products are sorted alphabetically ascending

  # TC-019 — Sort Z to A
  Scenario: Sort products alphabetically Z to A
    When the user sorts products by "Name (Z to A)"
    Then the products are sorted alphabetically descending

  # TC-020 — Sort price low to high
  Scenario: Sort products by price low to high
    When the user sorts products by "Price (low to high)"
    Then the products are sorted by price ascending

  # TC-021 — Sort price high to low
  Scenario: Sort products by price high to low
    When the user sorts products by "Price (high to low)"
    Then the products are sorted by price descending

  # TC-022 — Sort resets after navigation (known SauceDemo behaviour: sort is NOT persisted on back)
  Scenario: Sort order resets to default after returning from product detail
    When the user sorts products by "Name (Z to A)"
    And the user clicks on the first product name
    And the user clicks the back button
    Then the products are sorted alphabetically ascending
