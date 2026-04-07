package pages

import org.openqa.selenium.{By, WebDriver, WebElement}
import scala.jdk.CollectionConverters.*

class InventoryPage(driver: WebDriver):

  val url = "https://www.saucedemo.com/inventory.html"

  private val inventoryItems  = By.className("inventory_item")
  private val itemName        = By.className("inventory_item_name")
  private val itemPrice       = By.className("inventory_item_price")
  private val itemDesc        = By.className("inventory_item_desc")
  private val itemImg         = By.cssSelector(".inventory_item img")
  private val sortDropdown    = By.className("product_sort_container")
  private val cartBadge       = By.className("shopping_cart_badge")
  private val cartLink        = By.className("shopping_cart_link")
  private val burgerMenu      = By.id("react-burger-menu-btn")

  def open(): Unit = driver.get(url)

  def isOnInventoryPage: Boolean =
    driver.getCurrentUrl.contains("inventory.html")

  def getProductNames: List[String] =
    driver.findElements(itemName).asScala.map(_.getText).toList

  def getProductPrices: List[Double] =
    driver.findElements(itemPrice).asScala
      .map(_.getText.replace("$", "").toDouble)
      .toList

  def getAllItems: List[WebElement] =
    driver.findElements(inventoryItems).asScala.toList

  def clickProductByName(name: String): Unit =
    driver.findElements(itemName).asScala
      .find(_.getText == name)
      .getOrElse(throw RuntimeException(s"Product '$name' not found"))
      .click()

  def clickFirstProductName(): Unit =
    driver.findElements(itemName).asScala.head.click()

  def addToCartByName(name: String): Unit =
    val dataTest = "add-to-cart-" + name.toLowerCase.replace(" ", "-")
    driver.findElement(By.cssSelector(s"[data-test='$dataTest']")).click()

  def removeFromInventoryByName(name: String): Unit =
    val dataTest = "remove-" + name.toLowerCase.replace(" ", "-")
    driver.findElement(By.cssSelector(s"[data-test='$dataTest']")).click()

  def addAllToCart(): Unit =
    // Re-fetch buttons after each click to avoid stale element references on DOM re-render
    var remaining = driver.findElements(By.cssSelector(".btn_primary.btn_inventory")).asScala.toList
    while remaining.nonEmpty do
      remaining.head.click()
      remaining = driver.findElements(By.cssSelector(".btn_primary.btn_inventory")).asScala.toList

  def getButtonTextForProduct(name: String): String =
    val items = driver.findElements(inventoryItems).asScala
    items.find(el => el.findElement(itemName).getText == name) match
      case Some(item) => item.findElement(By.tagName("button")).getText
      case None       => throw RuntimeException(s"Product '$name' not found")

  def allButtonTexts: List[String] =
    driver.findElements(By.cssSelector(".btn_inventory")).asScala.map(_.getText).toList

  def getCartBadgeCount: Option[Int] =
    val badges = driver.findElements(cartBadge)
    if badges.isEmpty then None
    else Some(badges.get(0).getText.toInt)

  def isCartBadgeVisible: Boolean =
    !driver.findElements(cartBadge).isEmpty

  def navigateToCart(): Unit =
    driver.findElement(cartLink).click()

  def sortBy(option: String): Unit =
    val select = org.openqa.selenium.support.ui.Select(driver.findElement(sortDropdown))
    select.selectByVisibleText(option)

  def allImagesLoaded: Boolean =
    driver.findElements(itemImg).asScala.forall: img =>
      val src = img.getAttribute("src")
      src != null && src.nonEmpty && !src.contains("broken")

  def openBurgerMenu(): Unit =
    driver.findElement(burgerMenu).click()

  def refreshPage(): Unit =
    driver.navigate().refresh()

  def clickBack(): Unit =
    driver.navigate().back()
