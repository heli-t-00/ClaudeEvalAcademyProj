package pages

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
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
    val addTest    = "add-to-cart-" + name.toLowerCase.replace(" ", "-")
    val removeTest = "remove-"      + name.toLowerCase.replace(" ", "-")
    // Wait for the button to be clickable, then use JS click to bypass React event timing
    val btn = WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.elementToBeClickable(By.cssSelector(s"[data-test='$addTest']")))
    driver.asInstanceOf[org.openqa.selenium.JavascriptExecutor].executeScript("arguments[0].click()", btn)
    // Wait for React to flip the button to Remove — confirms cart state updated before next step
    WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(s"[data-test='$removeTest']")))

  def removeFromInventoryByName(name: String): Unit =
    val removeTest = "remove-"        + name.toLowerCase.replace(" ", "-")
    val addTest    = "add-to-cart-"   + name.toLowerCase.replace(" ", "-")
    val js  = driver.asInstanceOf[org.openqa.selenium.JavascriptExecutor]
    val btn = WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.elementToBeClickable(By.cssSelector(s"[data-test='$removeTest']")))
    js.executeScript("arguments[0].click()", btn)
    WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(s"[data-test='$addTest']")))

  def addAllToCart(): Unit =
    // Click every "Add to cart" button using data-test selectors for all 6 products.
    // Avoids an infinite loop in headless CI where .btn_primary.btn_inventory can mis-match.
    val allProducts = List(
      "sauce-labs-backpack", "sauce-labs-bike-light", "sauce-labs-bolt-t-shirt",
      "sauce-labs-fleece-jacket", "sauce-labs-onesie", "test.allthethings()-t-shirt-(red)"
    )
    val js = driver.asInstanceOf[org.openqa.selenium.JavascriptExecutor]
    for product <- allProducts do
      val addSel    = s"[data-test='add-to-cart-$product']"
      val removeSel = s"[data-test='remove-$product']"
      val btn = WebDriverWait(driver, java.time.Duration.ofSeconds(10))
        .until(ExpectedConditions.elementToBeClickable(By.cssSelector(addSel)))
      js.executeScript("arguments[0].click()", btn)
      WebDriverWait(driver, java.time.Duration.ofSeconds(10))
        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(removeSel)))

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
    val js  = driver.asInstanceOf[org.openqa.selenium.JavascriptExecutor]
    val btn = WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.elementToBeClickable(cartLink))
    js.executeScript("arguments[0].click()", btn)
    WebDriverWait(driver, java.time.Duration.ofSeconds(10))
      .until(ExpectedConditions.urlContains("cart.html"))

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
