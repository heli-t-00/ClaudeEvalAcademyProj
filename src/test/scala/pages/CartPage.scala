package pages

import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import scala.jdk.CollectionConverters.*

class CartPage(driver: WebDriver):

  val url = "https://www.saucedemo.com/cart.html"

  private val cartItems         = By.className("cart_item")
  private val cartItemName      = By.className("inventory_item_name")
  private val cartItemDesc      = By.className("inventory_item_desc")
  private val cartItemQty       = By.className("cart_quantity")
  private val cartItemPrice     = By.className("inventory_item_price")
  private val removeButton      = By.cssSelector(".btn_secondary.cart_button")
  private val continueShipping  = By.id("continue-shopping")
  private val checkoutButton    = By.id("checkout")
  private val cartBadge         = By.className("shopping_cart_badge")

  def isOnCartPage: Boolean =
    driver.getCurrentUrl.contains("cart.html")

  def getItemCount: Int =
    driver.findElements(cartItems).size()

  def isEmpty: Boolean =
    driver.findElements(cartItems).isEmpty

  def getItemNames: List[String] =
    driver.findElements(cartItemName).asScala.map(_.getText).toList

  def itemsHaveRequiredFields: Boolean =
    driver.findElements(cartItems).asScala.forall: item =>
      !item.findElements(cartItemName).isEmpty &&
      !item.findElements(cartItemDesc).isEmpty &&
      !item.findElements(cartItemQty).isEmpty &&
      !item.findElements(cartItemPrice).isEmpty

  def removeItemByName(name: String): Unit =
    val dataTest = "remove-" + name.toLowerCase.replace(" ", "-")
    driver.findElement(By.cssSelector(s"[data-test='$dataTest']")).click()

  def clickContinueShopping(): Unit =
    driver.findElement(continueShipping).click()

  def clickCheckout(): Unit =
    driver.findElement(checkoutButton).click()
    WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.urlContains("checkout-step-one"))

  def isCartBadgeVisible: Boolean =
    !driver.findElements(cartBadge).isEmpty
