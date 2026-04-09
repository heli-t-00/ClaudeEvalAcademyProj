package stepdefs

import context.TestContext
import io.cucumber.java.en.When
import org.openqa.selenium.{By, JavascriptExecutor}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}

class LogoutSteps(context: TestContext):

  private val logoutLink   = By.id("logout_sidebar_link")
  private val burgerButton = By.id("react-burger-menu-btn")

  @When("the user opens the burger menu")
  def openBurgerMenu(): Unit =
    val btn = WebDriverWait(context.driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.elementToBeClickable(burgerButton))
    context.driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click()", btn)
    WebDriverWait(context.driver, java.time.Duration.ofSeconds(15))
      .until(ExpectedConditions.elementToBeClickable(logoutLink))

  @When("the user clicks Logout")
  def clickLogout(): Unit =
    val el = context.driver.findElement(logoutLink)
    // JS click bypasses the react-burger-menu animation overlay
    context.driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click()", el)
    WebDriverWait(context.driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")))
