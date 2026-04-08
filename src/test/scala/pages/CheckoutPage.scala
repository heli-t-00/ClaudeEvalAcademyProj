package pages

import org.openqa.selenium.{By, JavascriptExecutor, WebDriver}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import scala.jdk.CollectionConverters.*

class CheckoutPage(driver: WebDriver):

  // Step 1 — Info
  private val firstNameField  = By.id("first-name")
  private val lastNameField   = By.id("last-name")
  private val zipField        = By.id("postal-code")
  private val continueButton  = By.id("continue")
  private val cancelButton    = By.id("cancel")
  private val errorMessage    = By.cssSelector("[data-test='error']")

  // Step 2 — Overview
  private val overviewItems   = By.className("cart_item")
  private val itemName        = By.className("inventory_item_name")
  private val subtotalLabel   = By.className("summary_subtotal_label")
  private val taxLabel        = By.className("summary_tax_label")
  private val totalLabel      = By.className("summary_total_label")
  private val finishButton    = By.id("finish")
  private val cancelOverview  = By.id("cancel")

  // Confirmation
  private val confirmHeader   = By.className("complete-header")
  private val backHomeButton  = By.id("back-to-products")

  private val wait = WebDriverWait(driver, java.time.Duration.ofSeconds(10))

  def isOnStep1: Boolean        = driver.getCurrentUrl.contains("checkout-step-one")
  def isOnStep2: Boolean        = driver.getCurrentUrl.contains("checkout-step-two")
  def isOnConfirmation: Boolean = driver.getCurrentUrl.contains("checkout-complete")

  def fillForm(firstName: String, lastName: String, zip: String): Unit =
    wait.until(ExpectedConditions.urlContains("checkout-step-one"))
    wait.until(ExpectedConditions.elementToBeClickable(firstNameField))
    // JS fill with native React setter (confirmed working — Python debug_test11).
    // Also clicks Continue inside the same script execution so React processes fill+click
    // atomically before the script returns.
    driver.asInstanceOf[JavascriptExecutor].executeScript(
      s"""function fill(id, val) {
        var el = document.getElementById(id);
        var s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
        s.call(el, val);
        el.dispatchEvent(new Event('input',  {bubbles: true}));
        el.dispatchEvent(new Event('change', {bubbles: true}));
      }
      fill('first-name',  '${firstName}');
      fill('last-name',   '${lastName}');
      fill('postal-code', '${zip}');
      document.getElementById('continue').click();"""
    )

  def clickContinue(): Unit =
    // fillForm already triggered Continue via JS.
    // Wait up to 3s for step-two; validation tests legitimately stay on step-one — swallow.
    try
      WebDriverWait(driver, java.time.Duration.ofSeconds(3))
        .until(ExpectedConditions.urlContains("checkout-step-two"))
    catch case _: Exception => ()

  def clickCancel(): Unit =
    val btn = wait.until(ExpectedConditions.elementToBeClickable(cancelButton))
    driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click()", btn)

  def clickCancelOnOverview(): Unit =
    wait.until(ExpectedConditions.urlContains("checkout-step-two"))
    val cancel = wait.until(ExpectedConditions.visibilityOfElementLocated(cancelOverview))
    driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click()", cancel)

  def clickFinish(): Unit =
    wait.until(ExpectedConditions.urlContains("checkout-step-two"))
    val finish = wait.until(ExpectedConditions.visibilityOfElementLocated(finishButton))
    // JS click bypasses potential React event-handler attachment timing issues
    driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click()", finish)

  def clickBackHome(): Unit =
    wait.until(ExpectedConditions.urlContains("checkout-complete"))
    val btn = wait.until(ExpectedConditions.elementToBeClickable(backHomeButton))
    driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].click()", btn)
    wait.until(ExpectedConditions.urlContains("inventory.html"))

  def getErrorMessage: String =
    wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText

  def isErrorDisplayed: Boolean =
    try
      wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage))
      true
    catch case _: Exception => false

  def getOverviewItemNames: List[String] =
    driver.findElements(overviewItems).asScala
      .map(_.findElement(itemName).getText)
      .toList

  def getSubtotal: Double = parsePrice(driver.findElement(subtotalLabel).getText)
  def getTax: Double      = parsePrice(driver.findElement(taxLabel).getText)
  def getTotal: Double    = parsePrice(driver.findElement(totalLabel).getText)

  def getConfirmationMessage: String =
    wait.until(ExpectedConditions.visibilityOfElementLocated(confirmHeader)).getText

  def getOverviewItemPrices: List[Double] =
    driver.findElements(By.className("inventory_item_price")).asScala
      .map(_.getText.replace("$", "").toDouble)
      .toList

  private def parsePrice(text: String): Double =
    text.replaceAll("[^0-9.]", "").toDouble
