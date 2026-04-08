package stepdefs

import context.TestContext
import io.cucumber.java.en.{Given, Then, When}
import org.junit.Assert.*
import pages.{CartPage, InventoryPage}

class CartSteps(context: TestContext):

  private def inventoryPage = InventoryPage(context.driver)
  private def cartPage      = CartPage(context.driver)

  @When("the user adds {string} to the cart")
  def addItemToCart(name: String): Unit =
    inventoryPage.addToCartByName(name)

  @When("the user adds all products to the cart")
  def addAllToCart(): Unit =
    inventoryPage.addAllToCart()

  @When("the user removes {string} from the inventory page")
  def removeFromInventory(name: String): Unit =
    inventoryPage.removeFromInventoryByName(name)

  @When("the user navigates to the cart")
  def navigateToCart(): Unit =
    inventoryPage.navigateToCart()

  @When("the user adds the item to the cart from the detail page")
  def addFromDetailPage(): Unit =
    import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
    val btn = WebDriverWait(context.driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.elementToBeClickable(org.openqa.selenium.By.cssSelector(".btn_primary")))
    btn.click()
    // Wait for badge to appear confirming React state updated
    WebDriverWait(context.driver, java.time.Duration.ofSeconds(5))
      .until(ExpectedConditions.presenceOfElementLocated(
        org.openqa.selenium.By.className("shopping_cart_badge")))

  @When("the user refreshes the page")
  def refreshPage(): Unit =
    inventoryPage.refreshPage()

  @When("the user clicks {string}")
  def clickButton(label: String): Unit =
    label match
      case "Continue Shopping" => cartPage.clickContinueShopping()
      case other               => throw RuntimeException(s"Unknown button: $other")

  @When("the user removes {string} from the cart")
  def removeFromCart(name: String): Unit =
    cartPage.removeItemByName(name)

  @Then("the cart badge shows {string}")
  def cartBadgeShows(expected: String): Unit =
    val actual = inventoryPage.getCartBadgeCount
    assertEquals(s"Cart badge mismatch", Some(expected.toInt), actual)

  @Then("the cart badge is not visible")
  def cartBadgeNotVisible(): Unit =
    assertFalse("Cart badge should not be visible", inventoryPage.isCartBadgeVisible)

  @Then("the button for {string} shows {string}")
  def buttonForProductShows(productName: String, expectedText: String): Unit =
    val actual = inventoryPage.getButtonTextForProduct(productName)
    assertTrue(
      s"Expected button '$expectedText' for '$productName' but got '$actual'",
      actual.equalsIgnoreCase(expectedText)
    )

  @Then("all product buttons show {string}")
  def allButtonsShow(expected: String): Unit =
    val allTexts = inventoryPage.allButtonTexts
    assertTrue(
      s"Not all buttons show '$expected': $allTexts",
      allTexts.forall(_.equalsIgnoreCase(expected))
    )

  @Then("the cart contains {string} items")
  def cartContainsItems(expected: String): Unit =
    assertEquals(s"Cart item count mismatch", expected.toInt, cartPage.getItemCount)

  @Then("the cart is empty")
  def cartIsEmpty(): Unit =
    assertTrue("Cart should be empty", cartPage.isEmpty)

  @Then("each cart item shows a name, description, quantity and price")
  def cartItemsHaveRequiredFields(): Unit =
    assertTrue("Cart items missing required fields", cartPage.itemsHaveRequiredFields)

  @Then("the user is back on the inventory page")
  def userIsBackOnInventoryPage(): Unit =
    assertTrue(
      s"Expected inventory page but got: ${context.driver.getCurrentUrl}",
      context.driver.getCurrentUrl.contains("inventory.html")
    )

  @Then("the user is on the cart page")
  def userIsOnCartPage(): Unit =
    assertTrue(
      s"Expected cart page but got: ${context.driver.getCurrentUrl}",
      cartPage.isOnCartPage
    )
