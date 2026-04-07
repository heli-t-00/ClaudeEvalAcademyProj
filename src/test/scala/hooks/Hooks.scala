package hooks

import context.TestContext
import io.cucumber.java.{After, Before}
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}

class Hooks(context: TestContext):

  @Before
  def setUp(): Unit =
    WebDriverManager.chromedriver().setup()
    val options = ChromeOptions()
    options.addArguments("--start-maximized")
    // options.addArguments("--headless")  // uncomment for headless mode
    context.driver = ChromeDriver(options)
    context.driver.manage().timeouts().implicitlyWait(
      java.time.Duration.ofSeconds(10)
    )

  @After
  def tearDown(): Unit =
    if context.driver != null then
      context.driver.quit()
