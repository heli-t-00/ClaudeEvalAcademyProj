package hooks

import context.TestContext
import io.cucumber.java.{After, Before, Scenario}
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.{OutputType, TakesScreenshot}

class Hooks(context: TestContext):

  @Before
  def setUp(): Unit =
    WebDriverManager.chromedriver().setup()
    val options = ChromeOptions()
    // In CI (GitHub Actions sets CI=true) run headless with sandbox flags required for Linux
    if sys.env.getOrElse("CI", "false") == "true" then
      options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                           "--disable-gpu", "--window-size=1920,1080")
    else
      options.addArguments("--start-maximized")
    context.driver = ChromeDriver(options)
    context.driver.manage().timeouts().implicitlyWait(
      java.time.Duration.ofSeconds(10)
    )

  @After
  def tearDown(scenario: Scenario): Unit =
    if context.driver != null then
      try
        val screenshot = context.driver.asInstanceOf[TakesScreenshot]
          .getScreenshotAs(OutputType.BYTES)
        val label = if scenario.isFailed then "FAILED" else "PASSED"
        scenario.attach(screenshot, "image/png", s"$label — ${scenario.getName}")
      catch case _: Exception => () // never block teardown due to screenshot error
      finally
        context.driver.quit()
