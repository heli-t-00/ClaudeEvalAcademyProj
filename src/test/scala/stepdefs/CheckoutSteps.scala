package stepdefs

import context.TestContext
import io.cucumber.java.en.{Given, Then, When}
import org.junit.Assert.*
import pages.{CartPage, CheckoutPage, InventoryPage}

class CheckoutSteps(context: TestContext):

  private def inventoryPage = InventoryPage(context.driver)
  private def cartPage      = CartPage(context.driver)
  private def checkoutPage  = CheckoutPage(context.driver)

  @Given("the user has {string} in the cart")
  def userHasItemInCart(itemName: String): Unit =
    inventoryPage.addToCartByName(itemName)

  @Given("the user clicks Checkout")
  def userClicksCheckout(): Unit =
    cartPage.clickCheckout()

  @When("the user enters checkout info: first name {string}, last name {string}, zip {string}")
  def enterCheckoutInfo(firstName: String, lastName: String, zip: String): Unit =
    checkoutPage.enterFirstName(firstName)
    checkoutPage.enterLastName(lastName)
    checkoutPage.enterZip(zip)

  @When("the user clicks Continue")
  def clickContinue(): Unit =
    checkoutPage.clickContinue()

  @When("the user clicks Finish")
  def clickFinish(): Unit =
    checkoutPage.clickFinish()

  @When("the user clicks Back Home")
  def clickBackHome(): Unit =
    checkoutPage.clickBackHome()

  @When("the user clicks Cancel on the checkout info page")
  def clickCancelOnStep1(): Unit =
    checkoutPage.clickCancel()

  @When("the user clicks Cancel on the overview page")
  def clickCancelOnOverview(): Unit =
    checkoutPage.clickCancelOnOverview()

  @Then("the order overview page is displayed")
  def overviewPageDisplayed(): Unit =
    assertTrue(
      s"Expected checkout step 2 but got: ${context.driver.getCurrentUrl}",
      checkoutPage.isOnStep2
    )

  @Then("the item {string} is listed in the overview")
  def itemListedInOverview(itemName: String): Unit =
    val items = checkoutPage.getOverviewItemNames
    assertTrue(
      s"Item '$itemName' not found in overview. Items: $items",
      items.contains(itemName)
    )

  @Then("the total price is correct")
  def totalPriceIsCorrect(): Unit =
    val subtotal = checkoutPage.getSubtotal
    val tax      = checkoutPage.getTax
    val total    = checkoutPage.getTotal
    val expected = BigDecimal(subtotal + tax).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    val actual   = BigDecimal(total).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    assertEquals(s"Total ($actual) does not equal subtotal + tax ($expected)", expected, actual, 0.01)

  @Then("the subtotal matches the sum of item prices")
  def subtotalMatchesItemPrices(): Unit =
    val itemPrices = checkoutPage.getOverviewItemPrices
    val subtotal   = checkoutPage.getSubtotal
    val expected   = BigDecimal(itemPrices.sum).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
    assertEquals(s"Subtotal ($subtotal) does not match sum of items ($expected)", expected, subtotal, 0.01)

  @Then("the order total equals subtotal plus tax")
  def orderTotalEqualsSubtotalPlusTax(): Unit =
    totalPriceIsCorrect()

  @Then("the order confirmation page is displayed")
  def confirmationPageDisplayed(): Unit =
    assertTrue(
      s"Expected confirmation page but got: ${context.driver.getCurrentUrl}",
      checkoutPage.isOnConfirmation
    )

  @Then("the confirmation message contains {string}")
  def confirmationMessageContains(expected: String): Unit =
    val msg = checkoutPage.getConfirmationMessage
    assertTrue(
      s"Confirmation message '$msg' does not contain '$expected'",
      msg.toLowerCase.contains(expected.toLowerCase)
    )

  @Then("a checkout error is displayed containing {string}")
  def checkoutErrorContains(expected: String): Unit =
    assertTrue("Checkout error not displayed", checkoutPage.isErrorDisplayed)
    assertTrue(
      s"Expected error containing '$expected' but got: '${checkoutPage.getErrorMessage}'",
      checkoutPage.getErrorMessage.toLowerCase.contains(expected.toLowerCase)
    )
