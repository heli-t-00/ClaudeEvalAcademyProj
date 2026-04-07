package runner

import io.cucumber.junit.{Cucumber, CucumberOptions}
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("src/test/resources/features"),
  glue     = Array("stepdefs", "hooks"),
  plugin   = Array(
    "pretty",
    "html:target/cucumber-reports/report.html",
    "json:target/cucumber-reports/report.json",
    "junit:target/cucumber-reports/report.xml"
  ),
  monochrome = true
)
class TestRunner
