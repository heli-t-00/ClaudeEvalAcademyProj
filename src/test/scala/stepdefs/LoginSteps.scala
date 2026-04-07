package stepdefs

import context.TestContext
import io.cucumber.java.en.{Given, Then, When}
import org.junit.Assert.*
import pages.LoginPage

class LoginSteps(context: TestContext):

  private def loginPage = LoginPage(context.driver)

  @Given("the user is on the login page")
  def userIsOnLoginPage(): Unit =
    loginPage.open()

  @When("the user enters username {string} and password {string}")
  def userEntersCredentials(username: String, password: String): Unit =
    if username.nonEmpty then loginPage.enterUsername(username)
    if password.nonEmpty then loginPage.enterPassword(password)

  @When("the user clicks the login button")
  def userClicksLogin(): Unit =
    loginPage.clickLogin()

  @Then("the user is redirected to the inventory page")
  def userIsOnInventoryPage(): Unit =
    assertTrue(
      s"Expected inventory page but got: ${context.driver.getCurrentUrl}",
      context.driver.getCurrentUrl.contains("inventory.html")
    )

  @Then("an error message is displayed containing {string}")
  def errorMessageContains(expected: String): Unit =
    assertTrue("Error message not displayed", loginPage.isErrorDisplayed)
    assertTrue(
      s"Expected error containing '$expected' but got: '${loginPage.getErrorMessage}'",
      loginPage.getErrorMessage.toLowerCase.contains(expected.toLowerCase)
    )

  @When("the user navigates directly to the inventory page")
  def navigateDirectlyToInventory(): Unit =
    context.driver.get("https://www.saucedemo.com/inventory.html")

  @Then("the user is redirected to the login page")
  def userIsOnLoginPage2(): Unit =
    assertTrue(
      s"Expected login page but got: ${context.driver.getCurrentUrl}",
      context.driver.getCurrentUrl == "https://www.saucedemo.com/"
        || context.driver.getCurrentUrl.contains("saucedemo.com")
        && !context.driver.getCurrentUrl.contains("inventory")
    )
