package stepdefs

import context.TestContext
import io.cucumber.java.en.{Given, Then, When}
import org.junit.Assert.*
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import pages.{InventoryPage, LoginPage}

class InventorySteps(context: TestContext):

  private def inventoryPage = InventoryPage(context.driver)
  private def loginPage     = LoginPage(context.driver)

  @Given("the user is logged in as {string}")
  def userIsLoggedInAs(username: String): Unit =
    val currentUrl = context.driver.getCurrentUrl
    // After logout the page is already the login page but React state may be stale.
    // Use JS fill + click (same technique that works for checkout) instead of sendKeys.
    if currentUrl.contains("saucedemo.com") && !currentUrl.contains("inventory") then
      WebDriverWait(context.driver, java.time.Duration.ofSeconds(10))
        .until(ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.id("login-button")))
      context.driver.asInstanceOf[JavascriptExecutor].executeScript(
        s"""function fill(id, val) {
          var el = document.getElementById(id);
          var s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
          s.call(el, val);
          el.dispatchEvent(new Event('input',  {bubbles: true}));
          el.dispatchEvent(new Event('change', {bubbles: true}));
        }
        fill('user-name', '$username');
        fill('password',  'secret_sauce');
        document.getElementById('login-button').click();"""
      )
    else
      loginPage.open()
      loginPage.login(username, "secret_sauce")
    WebDriverWait(context.driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.urlContains("inventory.html"))

  @Given("the user is on the inventory page")
  def userIsOnInventoryPage(): Unit =
    assertTrue(
      s"Expected inventory page but got: ${context.driver.getCurrentUrl}",
      inventoryPage.isOnInventoryPage
    )

  @Then("all product cards display a name, description, price and add to cart button")
  def allProductCardsHaveRequiredFields(): Unit =
    val items = inventoryPage.getAllItems
    assertFalse("No products found on inventory page", items.isEmpty)
    items.foreach: item =>
      assertFalse("Product missing name",        item.findElements(org.openqa.selenium.By.className("inventory_item_name")).isEmpty)
      assertFalse("Product missing description", item.findElements(org.openqa.selenium.By.className("inventory_item_desc")).isEmpty)
      assertFalse("Product missing price",       item.findElements(org.openqa.selenium.By.className("inventory_item_price")).isEmpty)
      assertFalse("Product missing button",      item.findElements(org.openqa.selenium.By.tagName("button")).isEmpty)

  @Then("no product images are broken")
  def noProductImagesAreBroken(): Unit =
    assertTrue("One or more product images failed to load", inventoryPage.allImagesLoaded)

  @When("the user clicks on the first product name")
  def clickFirstProduct(): Unit =
    inventoryPage.clickFirstProductName()

  @Then("the product detail page is displayed")
  def productDetailPageDisplayed(): Unit =
    assertTrue(
      s"Expected product detail page but got: ${context.driver.getCurrentUrl}",
      context.driver.getCurrentUrl.contains("inventory-item.html")
    )

  @Then("the product detail page shows a name, description, price and add to cart button")
  def productDetailHasRequiredFields(): Unit =
    val driver = context.driver
    assertFalse("Detail missing name",        driver.findElements(org.openqa.selenium.By.className("inventory_details_name")).isEmpty)
    assertFalse("Detail missing description", driver.findElements(org.openqa.selenium.By.className("inventory_details_desc")).isEmpty)
    assertFalse("Detail missing price",       driver.findElements(org.openqa.selenium.By.className("inventory_details_price")).isEmpty)
    assertFalse("Detail missing button",      driver.findElements(org.openqa.selenium.By.tagName("button")).isEmpty)

  @When("the user sorts products by {string}")
  def sortProductsBy(option: String): Unit =
    inventoryPage.sortBy(option)

  @Then("the products are sorted alphabetically ascending")
  def productsSortedAZ(): Unit =
    val names = inventoryPage.getProductNames
    assertEquals("Products not sorted A→Z", names.sorted, names)

  @Then("the products are sorted alphabetically descending")
  def productsSortedZA(): Unit =
    val names = inventoryPage.getProductNames
    assertEquals("Products not sorted Z→A", names.sorted.reverse, names)

  @Then("the products are sorted by price ascending")
  def productsSortedPriceLowHigh(): Unit =
    val prices = inventoryPage.getProductPrices
    assertEquals("Products not sorted by price low→high", prices.sorted, prices)

  @Then("the products are sorted by price descending")
  def productsSortedPriceHighLow(): Unit =
    val prices = inventoryPage.getProductPrices
    assertEquals("Products not sorted by price high→low", prices.sorted.reverse, prices)

  @When("the user clicks the back button")
  def clickBack(): Unit =
    inventoryPage.clickBack()
