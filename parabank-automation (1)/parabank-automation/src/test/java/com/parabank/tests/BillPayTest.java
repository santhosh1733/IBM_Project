package com.parabank.tests;

import com.parabank.listeners.ExtentManager;
import com.parabank.pages.AccountDetailsPage;
import com.parabank.pages.BillPayAccountOverviewPage;
import com.parabank.pages.BillPayPage;
import com.parabank.pages.LoginPage;
import com.parabank.utils.ConfigReader;
import com.parabank.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * M4 - Bill Pay automation.
 *
 * Five focused scripts are included: smoke/E2E success, negative account
 * mismatch validation, balance integration, transaction-history integration,
 * and amount-required validation.
 *
 * The application URL is read by BaseTest from config.properties. Login
 * credentials are read from config.properties so test classes do not contain
 * hard-coded usernames/passwords. Maven -D properties can still override the
 * config values for CI or another registered ParaBank user:
 *
 * mvn test -Dtest=BillPayTest -Dparabank.username=<user>
 * -Dparabank.password=<pass>
 */
public class BillPayTest extends BaseTest {

	private final Map<String, Map<String, String>> testDataById = new HashMap<>();

	@BeforeClass(alwaysRun = true)
	public void loadBillPayTestData() {
		List<Map<String, String>> rows = ExcelUtils.getSheetData(ConfigReader.get("testdata.path"), "BillPay");

		for (Map<String, String> row : rows) {
			String testCaseId = row.get("TestCaseId");
			if (testCaseId != null && !testCaseId.isBlank()) {
				testDataById.put(testCaseId, row);
			}
		}
	}

	@Test(groups = { "smoke", "e2e" }, description = "PB_AUTO_01 - Verify end-to-end successful Bill Payment")
	public void pbAuto01_verifyEndToEndSuccessfulBillPayment() {
		Map<String, String> data = data("PB_AUTO_01");

		info("Step 1: Login to ParaBank using valid credentials.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		String sourceAccount = overview.getFirstAccountId();
		info("Step 2: Navigate to Bill Pay and enter valid payee/payment details. " + "Payee=" + data.get("PayeeName")
				+ ", Amount=" + data.get("Amount") + ", From Account=" + sourceAccount + ".");
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, true);
		pass("Step 2 Passed: Bill Pay form populated with valid test data.");

		info("Step 3: Click Send Payment.");
		billPay.submitPayment();
		assertNoBackendError(billPay);
		pass("Step 3 Passed: Bill Pay request submitted without a backend application error.");

		info("Step 4: Verify the Bill Payment Complete confirmation.");
		Assert.assertTrue(billPay.isPaymentSuccessful(), "Bill Payment Complete confirmation was not displayed.");
		pass("Verification Passed: Bill Payment Complete confirmation is displayed.");

		info("Step 5: Verify confirmation payee name, payment amount and source account.");
		Assert.assertEquals(billPay.getConfirmedPayeeName(), data.get("PayeeName"),
				"Confirmed payee name is incorrect.");
		Assert.assertEquals(billPay.getConfirmedAmount(), formatCurrency(data.get("Amount")),
				"Confirmed payment amount is incorrect.");
		Assert.assertEquals(billPay.getConfirmedFromAccount(), sourceAccount, "Confirmed source account is incorrect.");
		pass("Verification Passed: Payee=" + data.get("PayeeName") + ", Amount=" + formatCurrency(data.get("Amount"))
				+ " and From Account=" + sourceAccount + " are correct.");
	}

	@Test(groups = { "functional",
			"negative" }, description = "PB_AUTO_02 - Verify Account and Verify Account mismatch validation")
	public void pbAuto02_verifyAccountNumberMismatchValidation() {
		Map<String, String> data = data("PB_AUTO_02");

		info("Step 1: Login to ParaBank using valid credentials.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		String sourceAccount = overview.getFirstAccountId();
		info("Step 2: Navigate to Bill Pay and enter valid payee details with mismatching "
				+ "Account and Verify Account values. Account=" + data.get("PayeeAccount") + ", Verify Account="
				+ data.get("VerifyAccount") + ".");
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, true);
		pass("Step 2 Passed: Mismatching account numbers and remaining valid data entered.");

		info("Step 3: Click Send Payment.");
		billPay.submitPayment();

		info("Step 4: Verify account-number mismatch validation is displayed.");
		Assert.assertTrue(billPay.isAccountMismatchErrorDisplayed(),
				"Account-number mismatch validation was not displayed.");
		pass("Verification Passed: Account-number mismatch validation is displayed.");

		info("Step 5: Verify the mismatch validation message text.");
		String expectedMessage = data.get("ExpectedMessage");
		Assert.assertEquals(billPay.getAccountMismatchErrorText(), expectedMessage,
				"Unexpected account-number mismatch message.");
		pass("Verification Passed: Validation message is '" + expectedMessage + "'.");
	}

	@Test(groups = { "integration" }, description = "PB_AUTO_03 - Verify account balance after successful Bill Payment")
	public void pbAuto03_verifyAccountBalanceAfterSuccessfulBillPayment() {
		Map<String, String> data = data("PB_AUTO_03");

		info("Step 1: Login to ParaBank using valid credentials.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		String sourceAccount = overview.getFirstAccountId();
		info("Step 2: Record the current balance of source account " + sourceAccount + ".");
		double balanceBefore = overview.getCurrentBalance(sourceAccount);
		pass("Step 2 Passed: Balance before payment = " + formatCurrency(balanceBefore) + ".");

		double paymentAmount = Double.parseDouble(data.get("Amount"));
		info("Step 3: Navigate to Bill Pay, enter valid data and submit payment of " + formatCurrency(paymentAmount)
				+ ".");
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, true).submitPayment();
		assertNoBackendError(billPay);
		Assert.assertTrue(billPay.isPaymentSuccessful(),
				"Bill payment did not complete, so balance integration cannot be verified.");
		pass("Step 3 Passed: Bill payment completed successfully.");

		info("Step 4: Return to Accounts Overview and read the updated balance.");
		BillPayAccountOverviewPage updatedOverview = billPay.goToAccountsOverview();
		double balanceAfter = updatedOverview.getCurrentBalance(sourceAccount);
		pass("Step 4 Passed: Balance after payment = " + formatCurrency(balanceAfter) + ".");

		info("Step 5: Verify source-account balance decreased exactly by the paid amount.");
		Assert.assertEquals(balanceAfter, balanceBefore - paymentAmount, 0.01,
				"Source-account balance was not reduced by exactly the paid amount.");
		pass("Verification Passed: Balance changed from " + formatCurrency(balanceBefore) + " to "
				+ formatCurrency(balanceAfter) + ", exactly matching payment amount " + formatCurrency(paymentAmount)
				+ ".");
	}

	@Test(groups = { "integration",
			"e2e" }, description = "PB_AUTO_04 - Verify successful Bill Payment in transaction history")
	public void pbAuto04_verifyBillPaymentTransactionHistory() {
		Map<String, String> data = data("PB_AUTO_04");

		info("Step 1: Login to ParaBank using valid credentials.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		String sourceAccount = overview.getFirstAccountId();
		info("Step 2: Navigate to Bill Pay and submit a valid payment. Payee=" + data.get("PayeeName") + ", Amount="
				+ data.get("Amount") + ", From Account=" + sourceAccount + ".");
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, true).submitPayment();
		assertNoBackendError(billPay);
		Assert.assertTrue(billPay.isPaymentSuccessful(),
				"Bill payment did not complete, so transaction history cannot be verified.");
		pass("Step 2 Passed: Bill payment completed successfully.");

		info("Step 3: Return to Accounts Overview and open source account " + sourceAccount + ".");
		BillPayAccountOverviewPage updatedOverview = billPay.goToAccountsOverview();
		AccountDetailsPage details = updatedOverview.openAccount(sourceAccount);
		pass("Step 3 Passed: Account Details / Account Activity page opened.");

		info("Step 4: Verify the transaction-history table is displayed.");
		Assert.assertTrue(details.isTransactionTableDisplayed(), "Transaction-history table was not displayed.");
		pass("Verification Passed: Transaction-history table is displayed.");

		info("Step 5: Verify the newly created Bill Pay transaction contains the expected "
				+ "payee and debit amount.");
		Assert.assertTrue(details.hasBillPaymentTransaction(data.get("PayeeName"), data.get("Amount")),
				"New Bill Pay transaction was not found with the expected payee and debit amount.");
		pass("Verification Passed: Transaction history contains Payee=" + data.get("PayeeName") + " and Amount="
				+ formatCurrency(data.get("Amount")) + ".");
	}

	@Test(groups = { "functional", "negative" }, description = "PB_AUTO_05 - Verify Amount mandatory validation")
	public void pbAuto05_verifyPaymentAmountMandatoryValidation() {
		Map<String, String> data = data("PB_AUTO_05");

		info("Step 1: Login to ParaBank using valid credentials.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		String sourceAccount = overview.getFirstAccountId();
		info("Step 2: Navigate to Bill Pay and enter valid payee/account details while leaving " + "Amount blank.");
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, false).leaveAmountBlank();
		pass("Step 2 Passed: Valid Bill Pay data entered and Amount left blank.");

		info("Step 3: Click Send Payment.");
		billPay.submitPayment();

		info("Step 4: Verify Amount mandatory validation is displayed.");
		Assert.assertTrue(billPay.isAmountRequiredErrorDisplayed(), "Amount-required validation was not displayed.");
		pass("Verification Passed: Amount-required validation is displayed.");

		info("Step 5: Verify the visible validation message belongs to the Amount field.");
		String validationText = billPay.getVisibleAmountValidationText();
		Assert.assertTrue(validationText.toLowerCase(Locale.ROOT).contains("amount"),
				"Unexpected Amount validation message: " + validationText);
		pass("Verification Passed: Amount validation message = '" + validationText + "'.");
	}

	private BillPayAccountOverviewPage loginAndGetOverview() {
		String username = System.getProperty("parabank.username", ConfigReader.get("parabank.username"));
		String password = System.getProperty("parabank.password", ConfigReader.get("parabank.password"));

		new LoginPage(driver).login(username, password);
		BillPayAccountOverviewPage overview = new BillPayAccountOverviewPage(driver).waitUntilLoaded();

		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Login did not reach the Accounts Overview page. "
						+ "Check parabank.username/parabank.password in config.properties "
						+ "or override them with Maven -D properties.");
		return overview;
	}

	private BillPayPage fillCompleteBillPayForm(BillPayAccountOverviewPage overview, Map<String, String> data,
			String sourceAccount, boolean enterAmount) {
		BillPayPage billPay = overview.goToBillPay()
				.fillPayeeDetails(data.get("PayeeName"), data.get("Street"), data.get("City"), data.get("State"),
						data.get("ZipCode"), data.get("Phone"), data.get("Email"), data.get("PayeeAccount"),
						data.get("VerifyAccount"))
				.selectFromAccount(sourceAccount);

		if (enterAmount) {
			billPay.enterAmount(data.get("Amount"));
		}
		return billPay;
	}

	private Map<String, String> data(String testCaseId) {
		Map<String, String> row = testDataById.get(testCaseId);
		if (row == null) {
			throw new IllegalStateException("BillPay test data row not found for TestCaseId: " + testCaseId);
		}
		return row;
	}

	private String formatCurrency(String rawAmount) {
		return String.format(Locale.US, "$%.2f", Double.parseDouble(rawAmount));
	}

	private String formatCurrency(double amount) {
		return String.format(Locale.US, "$%.2f", amount);
	}

	private void assertNoBackendError(BillPayPage billPay) {
		if (billPay.isBillPayErrorDisplayed()) {
			Assert.fail("ParaBank Bill Pay backend returned an application error: " + billPay.getBillPayErrorText());
		}
	}

	private void info(String message) {
		if (ExtentManager.getTest() != null) {
			ExtentManager.getTest().info(message);
		}
		logger.info(message);
	}

	private void pass(String message) {
		if (ExtentManager.getTest() != null) {
			ExtentManager.getTest().pass(message);
		}
		logger.info(message);
	}
}
