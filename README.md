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

6. **Tests executed and fixed to 100%** — Claude ran the full suite, diagnosed all failures, raised Jira bug tickets for each defect category, and iteratively fixed every issue until 44/44 scenarios passed

7. **Screenshots added** — Claude added automatic screenshot capture (pass and fail) embedded directly into the Cucumber HTML report as evidence of test completion

---

## Test Execution Results

| | |
|---|---|
| **Date executed** | 7 April 2026 |
| **Total scenarios** | 44 |
| **Passed** | 44 |
| **Failed** | 0 |
| **Pass rate** | 100% |
| **Execution time** | ~113 seconds |
| **Browser** | Chrome 146 |

### Results by Feature

| Feature | Scenarios | Result |
|---|---|---|
| Login | 12 | All passed |
| Inventory | 8 | All passed |
| Shopping Cart | 11 | All passed |
| Checkout | 9 | All passed |
| Logout | 3 | All passed |
| **Total** | **44** | **44 passed** |

Reports are generated to `target/cucumber-reports/` after each run:
- `report.html` — full HTML report with embedded screenshots per scenario
- `report.json` — machine-readable JSON
- `report.xml` — JUnit XML for CI integration

---

## Bug Tickets Raised During Execution

Three Jira bug tickets were raised during the execution phase against defects found in the automation:

| Ticket | Priority | Summary |
|---|---|---|
| SCRUM-91 | High | Checkout form fields not submitted — React controlled inputs not updated by standard Selenium `sendKeys` |
| SCRUM-92 | Medium | Cart badge count incorrect when adding multiple items — stale element reference after DOM re-render |
| SCRUM-93 | Medium | Re-login after logout fails — React state stale after session teardown |

### Key Technical Findings

- **SauceDemo uses React controlled inputs** — standard Selenium `sendKeys` and `clear()` do not update React's internal state. All form interactions require the native `HTMLInputElement` setter via JavaScript with `dispatchEvent('input')` and `dispatchEvent('change')` to trigger React's `onChange` handler
- **JS-triggered navigation is asynchronous** — after `executeScript()` fires a button click, Selenium returns immediately before navigation completes. Explicit `WebDriverWait` for the target URL is required before subsequent steps
- **React event handler attachment timing** — `elementToBeClickable` passes as soon as the DOM element is visible and enabled, but React may not have attached its `onClick` handler yet. JavaScript click (`arguments[0].click()`) bypasses this and fires reliably
- **Implicit + explicit wait conflict** — mixing `implicitlyWait` with `WebDriverWait` on `findElement`-based conditions can cause compounded timeouts. URL-based `ExpectedConditions` (`urlContains`) are unaffected and preferred for navigation waits

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
| Login (successful, failed, locked-out, SQL injection, session security) | 12 |
| Inventory (product cards, images, sorting, product detail) | 8 |
| Shopping Cart (add single/multiple/all, remove, persist, empty state) | 11 |
| Checkout (valid info, overview, totals, finish, cancel, validation errors) | 9 |
| Logout (burger menu, session invalidation, re-login) | 3 |
| **Total** | **44** |

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
        │   └── Hooks.scala             # @Before / @After — setup, teardown, screenshots
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
Each scenario in the HTML report includes an embedded screenshot taken at completion (labelled PASSED or FAILED).

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
- **JavaScript interactions** — buttons in SauceDemo's React app require `JavascriptExecutor` clicks to ensure React event handlers fire reliably; form inputs require the native `HTMLInputElement` setter to update React state
- **Screenshot evidence** — the `@After` hook captures a PNG screenshot after every scenario and embeds it in the Cucumber HTML report, providing visual evidence of the application state at test completion

---

## Predictive Risk & Stability Analysis

A risk-based analysis was performed across all 13 test runs to identify high-risk modules and prioritise future test execution.

### Module Risk Ranking

| Module | Total Failures | Peak Failure Rate | Risk Level |
|---|---|---|---|
| **Checkout** | 34 | 54% | CRITICAL |
| **Logout** | 3 | 23% | MEDIUM |
| **Cart** | 3 | 15% | MEDIUM |
| Login | 0 | 0% | STABLE |
| Inventory | 0 | 0% | STABLE |

### Prioritised Test Execution Plan

| Priority | Module | Scenarios | When to Run |
|---|---|---|---|
| P1 | Checkout | Complete purchase, Finish button, Cancel on overview | Every build / PR merge |
| P2 | Checkout | Validation scenarios (missing fields) | Daily regression |
| P2 | Logout | Re-login after logout | Daily regression |
| P3 | Cart | Add/remove multiple items | Weekly regression |
| P4 | Checkout, Logout | Cancel on step 1, burger menu logout | Release regression only |
| P5 | Login, Inventory | All 12 login + 8 inventory scenarios | Release regression only |

### Key Technical Findings

- **React controlled inputs** are the #1 systemic failure cause — standard Selenium `sendKeys` does not update React state. Requires native `HTMLInputElement` setter via JS + `dispatchEvent`
- **Async JS navigation** — `executeScript()` returns before browser navigation completes; explicit `WebDriverWait` for target URL is required before subsequent steps
- **React event handler timing** — `elementToBeClickable` passes before React binds `onClick`; JS click (`arguments[0].click()`) bypasses this reliably
- **Stale element references** — cart DOM re-renders after each add/remove; `By.tagName("button")` breaks; `data-test` attribute selectors are stable
- **Login and Inventory are fully stable** — 0 failures across all 13 runs; candidates for reduced regression frequency

---

## AI Tool Used

This project was built using **[Claude Code](https://claude.ai/code)** by Anthropic — a terminal-based AI coding assistant. The entire workflow from Jira analysis through to test execution, bug triage, risk analysis, and GitHub push was completed in a single conversation session without writing a single line of code manually.
