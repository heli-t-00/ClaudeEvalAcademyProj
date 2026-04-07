package pages

import org.openqa.selenium.{By, WebDriver}

class LoginPage(driver: WebDriver):

  private val usernameField  = By.id("user-name")
  private val passwordField  = By.id("password")
  private val loginButton    = By.id("login-button")
  private val errorMessage   = By.cssSelector("[data-test='error']")

  val url = "https://www.saucedemo.com"

  def open(): Unit =
    driver.get(url)
    org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(loginButton))

  def enterUsername(username: String): Unit =
    driver.findElement(usernameField).clear()
    driver.findElement(usernameField).sendKeys(username)

  def enterPassword(password: String): Unit =
    driver.findElement(passwordField).clear()
    driver.findElement(passwordField).sendKeys(password)

  def clickLogin(): Unit =
    driver.findElement(loginButton).click()

  def login(username: String, password: String): Unit =
    enterUsername(username)
    enterPassword(password)
    clickLogin()

  def getErrorMessage: String =
    driver.findElement(errorMessage).getText

  def isErrorDisplayed: Boolean =
    driver.findElements(errorMessage).size() > 0
