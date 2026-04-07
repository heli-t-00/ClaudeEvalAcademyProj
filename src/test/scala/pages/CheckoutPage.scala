package pages

import org.openqa.selenium.{By, WebDriver}
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

  def isOnStep1: Boolean =
    driver.getCurrentUrl.contains("checkout-step-one")

  def isOnStep2: Boolean =
    driver.getCurrentUrl.contains("checkout-step-two")

  def isOnConfirmation: Boolean =
    driver.getCurrentUrl.contains("checkout-complete")

  def enterFirstName(value: String): Unit =
    driver.findElement(firstNameField).clear()
    driver.findElement(firstNameField).sendKeys(value)

  def enterLastName(value: String): Unit =
    driver.findElement(lastNameField).clear()
    driver.findElement(lastNameField).sendKeys(value)

  def enterZip(value: String): Unit =
    driver.findElement(zipField).clear()
    driver.findElement(zipField).sendKeys(value)

  def clickContinue(): Unit =
    driver.findElement(continueButton).click()

  def clickCancel(): Unit =
    driver.findElement(cancelButton).click()

  def clickCancelOnOverview(): Unit =
    driver.findElement(cancelOverview).click()

  def clickFinish(): Unit =
    driver.findElement(finishButton).click()

  def clickBackHome(): Unit =
    driver.findElement(backHomeButton).click()

  def getErrorMessage: String =
    driver.findElement(errorMessage).getText

  def isErrorDisplayed: Boolean =
    !driver.findElements(errorMessage).isEmpty

  def getOverviewItemNames: List[String] =
    driver.findElements(overviewItems).asScala
      .map(_.findElement(itemName).getText)
      .toList

  def getSubtotal: Double =
    parsePrice(driver.findElement(subtotalLabel).getText)

  def getTax: Double =
    parsePrice(driver.findElement(taxLabel).getText)

  def getTotal: Double =
    parsePrice(driver.findElement(totalLabel).getText)

  def getConfirmationMessage: String =
    driver.findElement(confirmHeader).getText

  def getOverviewItemPrices: List[Double] =
    driver.findElements(By.className("inventory_item_price")).asScala
      .map(el => el.getText.replace("$", "").toDouble)
      .toList

  private def parsePrice(text: String): Double =
    text.replaceAll("[^0-9.]", "").toDouble
