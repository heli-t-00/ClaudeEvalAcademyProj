# ClaudeEvalAcademyProj

A Scala 3 test automation framework for [SauceDemo](https://www.saucedemo.com) — an e-commerce demo application — built with Selenium WebDriver and Cucumber BDD. This project was created end-to-end using **Claude Code (Anthropic AI)** as part of a QA evaluation exercise.

---

## How This Was Created with AI

This project was generated entirely through a conversation with **Claude Code (claude-sonnet-4-6)**, Anthropic's AI-powered coding assistant. No code was written by hand.

The session followed this workflow:

1. **Connected to Jira** — Claude used the Atlassian MCP integration to pull all 28 issues from the SCRUM project backlog at `testingclaude.atlassian.net`

2. **QA Analysis** — Acting as a senior QA engineer, Claude analysed the backlog and produced:
   - An executive summary with KPIs and observations
   - Identification of coverage gaps (no password reset, no payment failure, no accessibility epics)
   - A risk matrix with likelihood, impact and recommendations

3. **Excel Report generated** — Claude created a formatted multi-sheet Excel workbook (`SCRUM_Backlog_QA_Report.xlsx`) containing:
   - Executive Summary
   - Epics
   - Full Backlog
   - 60 Test Cases (mapped to stories)
   - 99 Test Data rows (valid, invalid, edge case, security, accessibility)
   - QA Risk Matrix

4. **Test Automation scaffolded** — Claude designed and wrote all source files for this project from scratch, including the Page Object Model, Cucumber feature files, step definitions, hooks, and test runner

5. **GitHub repository created** — Claude installed the `gh` CLI, initialised the git repo, and pushed everything to GitHub under the `heli-t-00` account

---

## Project Overview

| | |
|---|---|
| **App under test** | [SauceDemo](https://www.saucedemo.com) |
| **Language** | Scala 3.3.7 |
| **Build tool** | sbt 1.12.5 |
| **Test framework** | Cucumber 7.20.1 (cucumber-java) |
| **Browser automation** | Selenium WebDriver 4.27.0 |
| **Driver management** | WebDriverManager 5.9.2 |
| **DI** | PicoContainer |
| **Assertions** | JUnit 4 |

---

## Test Coverage

| Feature | Scenarios |
|---|---|
| Login (successful, failed, locked-out, SQL injection) | 11 |
| Inventory / Product Listings | 8 |
| Sorting (A-Z, Z-A, price low/high) | included above |
| Shopping Cart (add, remove, persist) | 11 |
| Checkout (info, overview, complete, cancel) | 9 |
| Logout & session security | 3 |
| **Total** | **42** |

---

## Project Structure

```
ClaudeEvalAcademyProj/
├── build.sbt
├── project/
│   └── build.properties
└── src/test/
    ├── resources/
    │   └── features/
    │       ├── login.feature
    │       ├── inventory.feature
    │       ├── cart.feature
    │       ├── checkout.feature
    │       └── logout.feature
    └── scala/
        ├── context/
        │   └── TestContext.scala        # Shared WebDriver state via PicoContainer DI
        ├── hooks/
        │   └── Hooks.scala             # @Before / @After setup and teardown
        ├── pages/                      # Page Object Model
        │   ├── LoginPage.scala
        │   ├── InventoryPage.scala
        │   ├── CartPage.scala
        │   └── CheckoutPage.scala
        ├── stepdefs/                   # Cucumber step definitions
        │   ├── LoginSteps.scala
        │   ├── InventorySteps.scala
        │   ├── CartSteps.scala
        │   ├── CheckoutSteps.scala
        │   └── LogoutSteps.scala
        └── runner/
            └── TestRunner.scala        # JUnit runner with CucumberOptions
```

---

## Prerequisites

- JDK 17+
- sbt 1.12.5+
- Google Chrome (latest)

---

## Running the Tests

```bash
# Run all tests
sbt test

# Continuous test run (re-runs on file save)
sbt ~test
```

HTML, JSON, and JUnit XML reports are generated to `target/cucumber-reports/` after each run.

### Headless mode

To run without opening a browser window, uncomment the headless flag in [Hooks.scala](src/test/scala/hooks/Hooks.scala):

```scala
options.addArguments("--headless")
```

---

## Design Decisions

- **Page Object Model** — each page of the app has a dedicated Scala class encapsulating its selectors and interactions, keeping step definitions clean and readable
- **PicoContainer DI** — the `TestContext` class holds the shared `WebDriver` instance and is injected into all step definition classes automatically, avoiding static state
- **cucumber-java** — used over cucumber-scala for stability with Scala 3; Java annotations (`@Given`, `@When`, `@Then`) work seamlessly via Scala/Java interop
- **WebDriverManager** — automatically resolves and downloads the correct ChromeDriver version, removing manual driver management

---

## AI Tool Used

This project was built using **[Claude Code](https://claude.ai/code)** by Anthropic — a terminal-based AI coding assistant. The entire workflow from Jira analysis through to GitHub push was completed in a single conversation session without writing a single line of code manually.
