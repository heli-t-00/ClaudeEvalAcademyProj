package stepdefs

import context.TestContext
import io.cucumber.java.en.{Then, When}
import org.junit.Assert.*
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import pages.LoginPage

class LogoutSteps(context: TestContext):

  private val logoutLink   = By.id("logout_sidebar_link")
  private val burgerButton = By.id("react-burger-menu-btn")

  @When("the user opens the burger menu")
  def openBurgerMenu(): Unit =
    context.driver.findElement(burgerButton).click()
    WebDriverWait(context.driver, java.time.Duration.ofSeconds(5))
      .until(ExpectedConditions.elementToBeClickable(logoutLink))

  @When("the user clicks Logout")
  def clickLogout(): Unit =
    context.driver.findElement(logoutLink).click()

  @Then("the user is on the login page")
  def userIsOnLoginPage(): Unit =
    val url = context.driver.getCurrentUrl
    assertTrue(
      s"Expected login page but got: $url",
      url == "https://www.saucedemo.com/" || url.endsWith("saucedemo.com")
    )
