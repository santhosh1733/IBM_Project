# ParaBank UI Automation Framework

Java + Selenium 4 + TestNG + Apache POI + Maven framework for automating
[ParaBank](https://parabank.parasoft.com/parabank/index.htm).

## Tech Stack
- Java 17
- Selenium 4.23.0
- TestNG 7.10.2
- Apache POI 5.3.0 (Excel-driven test data)
- WebDriverManager (auto driver binaries — no manual chromedriver downloads)
- Extent Reports (HTML reporting)
- Log4j2 (logging)
- Maven (build/dependency management)

## Project Structure
```
parabank-automation/
├── pom.xml
├── testng.xml                          # main suite, parallel="classes"
├── src/
│   ├── main/java/com/parabank/
│   │   ├── base/
│   │   │   └── DriverFactory.java      # ThreadLocal WebDriver, browser setup
│   │   ├── listeners/
│   │   │   ├── ExtentManager.java      # singleton ExtentReports instance
│   │   │   └── TestListener.java       # ITestListener -> logs + report + screenshots
│   │   ├── pages/                      # Page Object Model
│   │   │   ├── BasePage.java
│   │   │   ├── LoginPage.java
│   │   │   ├── AccountOverviewPage.java
│   │   │   ├── OpenNewAccountPage.java        (M2 — scaffold)
│   │   │   ├── TransferFundsPage.java         (M3 — scaffold)
│   │   │   ├── FindTransactionsPage.java      (M3 — scaffold)
│   │   │   ├── BillPayPage.java               (M4 — scaffold)
│   │   │   ├── UpdateContactInfoPage.java     (M5 — scaffold)
│   │   │   └── RequestLoanPage.java           (M5 — FULLY BUILT reference)
│   │   └── utils/
│   │       ├── ConfigReader.java
│   │       ├── WaitUtils.java
│   │       └── ExcelUtils.java         # Apache POI reader/writer
│   └── test/java/com/parabank/tests/
│       ├── BaseTest.java               # @BeforeMethod/@AfterMethod, screenshots
│       ├── LoginTest.java                     (M1 — starter)
│       ├── AccountOverviewTest.java           (M1/M2 — starter)
│       ├── TransferFundsTest.java             (M3 — starter)
│       ├── BillPayTest.java                   (M4 — starter)
│       ├── FindTransactionsTest.java          (M3 — starter)
│       ├── UpdateContactInfoTest.java         (M5 — starter)
│       └── RequestLoanTest.java               (M5 — FULLY BUILT reference)
└── src/test/resources/
    ├── config.properties
    ├── log4j2.xml
    └── testdata/TestData.xlsx          # sample data-driven test sheets
```

## Setup

1. **Install JDK 17+** and **Maven 3.8+**
2. Clone/unzip the project
3. Fill in real ParaBank test credentials:
   - Register a user at https://parabank.parasoft.com/parabank/register.htm
   - Replace `"your_test_username"` / `"your_test_password"` placeholders in
     each `*Test.java` class (or, better — move them into `config.properties`
     and read via `ConfigReader.get(...)`, which is a good first improvement
     for whoever owns `LoginTest`)
4. Run the full suite:
   ```bash
   mvn clean test
   ```
5. Run a single module while developing:
   ```bash
   mvn test -Dtest=RequestLoanTest
   ```

> **Note on this sandbox:** this project was scaffolded in an environment
> without access to Maven Central, so `mvn compile` could not be verified
> here. Run `mvn clean compile` locally as your first step — if anything
> doesn't compile, it's most likely a locator or import fix, not a structural
> issue with the framework.

## How the framework is wired together

- **DriverFactory** uses `ThreadLocal<WebDriver>` — this is what makes
  `parallel="classes"` in `testng.xml` safe. Never make WebDriver a plain
  static field.
- **BaseTest** handles driver init/teardown and screenshot-on-failure for
  every test class. Don't duplicate this logic in your own test class.
- **BasePage** centralizes `click()`, `type()`, `getText()` with built-in
  explicit waits via `WaitUtils`. Page objects should never call
  `element.click()` directly — always go through these helpers.
- **ExcelUtils** reads `TestData.xlsx` into `List<Map<String,String>>` or a
  TestNG `Object[][]` for `@DataProvider` — see `RequestLoanTest.loanData()`
  for the pattern.
- **RequestLoanPage.java** + **RequestLoanTest.java** are fully implemented —
  use these two files as the template for your own module. Same layering,
  same naming conventions, same no-raw-Selenium-in-tests rule.

## Team assignments

| Member | Module(s) | Files to complete |
|---|---|---|
| M1 | Login/Auth + Registration | `LoginTest.java`, `LoginPage.java` (add RegistrationPage) |
| M2 | Open New Account + Account Overview | `AccountOverviewTest.java`, `OpenNewAccountPage.java` |
| M3 | Transfer Funds + Find Transactions | `TransferFundsTest.java`, `FindTransactionsTest.java` |
| M4 | Bill Pay | `BillPayTest.java`, `BillPayPage.java` |
| M5 | Update Contact Info + Request Loan | `UpdateContactInfoTest.java` (Request Loan already done — use it as your reference) |

**Before writing test logic:** every page object's locators are
best-guess/starter scaffolds except `LoginPage`, `AccountOverviewPage`, and
`RequestLoanPage`. Right-click → Inspect on the live ParaBank page to confirm
each `@FindBy` locator before relying on it — I've left `// TODO` comments
marking exactly what to verify/add in each file.

## Git workflow suggestion
- One feature branch per module owner (`feature/transfer-funds`, etc.)
- PR review by team lead before merge to `main`
- Merge order matters: `AccountOverviewPage` and `LoginPage` are shared
  dependencies — get those stable in `main` first (M1) so M2–M5 aren't
  building against a moving target.

## Reports & Logs
- **Extent HTML report**: `test-output/ExtentReport.html` (path configurable via
  `report.path` in `config.properties`). Generated automatically by
  `com.parabank.listeners.TestListener` + `ExtentManager` — no manual wiring
  needed in individual test classes.
  - Every `@Test` becomes an entry with pass/fail/skip status
  - Failed tests get a screenshot embedded directly in the report (base64,
    so the report stays a single portable HTML file you can email/share)
  - Tests are auto-categorized by test class name (module), so you can filter
    the report by module in the Extent UI
  - Thread-safe via `ThreadLocal<ExtentTest>` — safe under `parallel="classes"`
- Screenshots (file-based, for CI artifact upload): `test-output/screenshots/`
- Logs: `test-output/logs/automation.log`

### How the listener is wired
- Registered at suite level in `testng.xml` (`<listeners>` block) **and** via
  `@Listeners(TestListener.class)` on `BaseTest` — the second one is a
  fallback so the report still works if someone runs a class directly
  (`mvn test -Dtest=RequestLoanTest`) without going through `testng.xml`.
- You don't need to add anything to your own test class to get report
  entries — extending `BaseTest` is enough.
- `ExtentManager.getReportInstance()` is a singleton — don't call
  `new ExtentReports()` anywhere else, or you'll end up with multiple report
  files fighting over the same output path.

## Next steps for the team
1. Each member verifies their page object's locators against the live site
2. Each member implements the `// TODO` test methods following the
   RequestLoanTest pattern
3. Wire up Excel-driven data provider tests for your module (see
   `TestData.xlsx` sheets already scaffolded for Login, TransferFunds, BillPay)
4. Team lead sets up GitHub Actions CI once individual suites are green locally
