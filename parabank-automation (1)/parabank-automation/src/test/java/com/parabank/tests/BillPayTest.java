package com.parabank.tests;

import com.parabank.listeners.ExtentManager;
import com.parabank.pages.AccountDetailsPage;
import com.parabank.pages.BillPayAccountOverviewPage;
import com.parabank.pages.BillPayFindTransactionsPage;
import com.parabank.pages.BillPayOpenNewAccountPage;
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
 * Ten focused scripts are included across smoke, functional, integration,
 * regression, system and end-to-end coverage. The added scenarios verify
 * cross-module account consistency, Find Transactions integration, new-account
 * to Bill Pay flow, cumulative payment impact, and transaction persistence
 * across logout/login.
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

	@Test(groups = { "smoke", "regression" },
			description = "PB_AUTO_06 - Verify Bill Pay module loads with all active source accounts")
	public void pbAuto06_verifyBillPayModuleAndSourceAccountConsistency() {
		data("PB_AUTO_06"); // Keeps this scenario traceable in the BillPay Excel sheet.

		info("Step 1: Login to ParaBank using valid credentials.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		info("Step 2: Capture all active account IDs from Accounts Overview.");
		List<String> overviewAccountIds = overview.getAllAccountIds();
		Assert.assertFalse(overviewAccountIds.isEmpty(), "No active accounts are available for Bill Pay smoke testing.");
		pass("Step 2 Passed: Active accounts found = " + overviewAccountIds + ".");

		info("Step 3: Navigate to Bill Pay and verify the critical Bill Pay controls are displayed.");
		BillPayPage billPay = overview.goToBillPay().waitUntilLoaded();
		Assert.assertTrue(billPay.isCriticalBillPayFormDisplayed(),
				"One or more critical Bill Pay controls are not displayed.");
		pass("Step 3 Passed: Bill Pay form loaded with critical controls.");

		info("Step 4: Read all source-account options from the Bill Pay From Account dropdown.");
		List<String> billPayAccountIds = billPay.getFromAccountOptions();
		pass("Step 4 Passed: Bill Pay source-account options = " + billPayAccountIds + ".");

		info("Step 5: Verify Accounts Overview and Bill Pay expose the same active account IDs.");
		Assert.assertEquals(billPayAccountIds.size(), overviewAccountIds.size(),
				"Bill Pay source-account count does not match Accounts Overview.");
		Assert.assertTrue(billPayAccountIds.containsAll(overviewAccountIds)
						&& overviewAccountIds.containsAll(billPayAccountIds),
				"Bill Pay source accounts are not synchronized with Accounts Overview.");
		pass("Verification Passed: All active Accounts Overview IDs are available in Bill Pay.");
	}

	@Test(groups = { "integration", "regression" },
			description = "PB_AUTO_07 - Verify Bill Payment is searchable in Find Transactions")
	public void pbAuto07_verifyBillPaymentThroughFindTransactions() {
		Map<String, String> data = data("PB_AUTO_07");

		info("Step 1: Login and capture the source account.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		String sourceAccount = overview.getFirstAccountId();
		pass("Step 1 Passed: Source account = " + sourceAccount + ".");

		info("Step 2: Submit a Bill Payment using unique payee and amount test data.");
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, true).submitPayment();
		assertNoBackendError(billPay);
		Assert.assertTrue(billPay.isPaymentSuccessful(),
				"Bill payment did not complete, so Find Transactions integration cannot be verified.");
		pass("Step 2 Passed: Bill payment completed for Payee=" + data.get("PayeeName")
				+ ", Amount=" + formatCurrency(data.get("Amount")) + ".");

		info("Step 3: Return to Accounts Overview and navigate to Find Transactions.");
		BillPayAccountOverviewPage updatedOverview = billPay.goToAccountsOverview();
		BillPayFindTransactionsPage findTransactions = updatedOverview.goToBillPayFindTransactions();
		pass("Step 3 Passed: Find Transactions page opened.");

		info("Step 4: Select the same source account used for Bill Pay and search by the exact amount.");
		findTransactions.selectAccount(sourceAccount);
		Assert.assertEquals(findTransactions.getSelectedAccountId(), sourceAccount,
				"Find Transactions is not searching the same account used for Bill Pay.");
		findTransactions.searchByAmount(data.get("Amount"));
		pass("Step 4 Passed: Account " + sourceAccount + " selected and search submitted for amount "
				+ formatCurrency(data.get("Amount")) + ".");

		info("Step 5: Verify the search result contains the Bill Pay payee and debit amount.");
		Assert.assertTrue(findTransactions.hasTransactionContaining(data.get("PayeeName"), data.get("Amount")),
				"Find Transactions did not return the newly created Bill Pay transaction.");
		pass("Verification Passed: Find Transactions contains Payee=" + data.get("PayeeName")
				+ " and Amount=" + formatCurrency(data.get("Amount")) + ".");
	}

	@Test(groups = { "integration", "e2e" },
			description = "PB_AUTO_08 - Verify Open New Account to Bill Pay end-to-end flow")
	public void pbAuto08_verifyNewAccountToBillPayEndToEndFlow() {
		Map<String, String> data = data("PB_AUTO_08");

		info("Step 1: Login and select an existing account to fund a new account.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		String fundingAccount = overview.getFirstAccountId();
		pass("Step 1 Passed: Funding account = " + fundingAccount + ".");

		info("Step 2: Open a new " + data.get("AccountType") + " account.");
		BillPayOpenNewAccountPage openNewAccount = overview.goToBillPayOpenNewAccount()
				.openAccount(data.get("AccountType"), fundingAccount);
		String newAccountId = openNewAccount.getNewAccountId();
		Assert.assertFalse(newAccountId.isBlank(), "New account ID was not created.");
		pass("Step 2 Passed: New account created = " + newAccountId + ".");

		info("Step 3: Return to Accounts Overview and verify the new account is listed.");
		BillPayAccountOverviewPage updatedOverview = openNewAccount.goToAccountsOverview();
		Assert.assertTrue(updatedOverview.getAllAccountIds().contains(newAccountId),
				"New account is not visible in Accounts Overview.");
		double balanceBefore = updatedOverview.getCurrentBalance(newAccountId);
		pass("Step 3 Passed: New account is active. Balance before Bill Pay = "
				+ formatCurrency(balanceBefore) + ".");

		info("Step 4: Pay a bill from the newly created account.");
		double paymentAmount = Double.parseDouble(data.get("Amount"));
		BillPayPage billPay = fillCompleteBillPayForm(updatedOverview, data, newAccountId, true).submitPayment();
		assertNoBackendError(billPay);
		Assert.assertTrue(billPay.isPaymentSuccessful(),
				"Bill payment from the newly created account did not complete.");
		Assert.assertEquals(billPay.getConfirmedFromAccount(), newAccountId,
				"Confirmation did not show the newly created account as the payment source.");
		pass("Step 4 Passed: Bill payment completed from new account " + newAccountId + ".");

		info("Step 5: Verify the new account balance decreased by the Bill Pay amount.");
		BillPayAccountOverviewPage afterPayment = billPay.goToAccountsOverview();
		double balanceAfter = afterPayment.getCurrentBalance(newAccountId);
		Assert.assertEquals(balanceAfter, balanceBefore - paymentAmount, 0.01,
				"New account balance was not reduced by the Bill Pay amount.");
		pass("Step 5 Passed: New-account balance changed from " + formatCurrency(balanceBefore)
				+ " to " + formatCurrency(balanceAfter) + ".");

		info("Step 6: Open the new account and verify its Bill Pay transaction history.");
		AccountDetailsPage details = afterPayment.openAccount(newAccountId);
		Assert.assertTrue(details.hasBillPaymentTransaction(data.get("PayeeName"), data.get("Amount")),
				"Bill Pay transaction was not found in the newly created account.");
		pass("Verification Passed: Open New Account -> Bill Pay -> Balance -> Transaction History flow is complete.");
	}

	@Test(groups = { "integration", "regression" },
			description = "PB_AUTO_09 - Verify two Bill Payments cause cumulative balance deduction")
	public void pbAuto09_verifyCumulativeBalanceAfterTwoBillPayments() {
		Map<String, String> data = data("PB_AUTO_09");

		info("Step 1: Login and record the initial source-account balance.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		String sourceAccount = overview.getFirstAccountId();
		double balanceBefore = overview.getCurrentBalance(sourceAccount);
		pass("Step 1 Passed: Initial balance = " + formatCurrency(balanceBefore) + ".");

		info("Step 2: Submit the first Bill Payment.");
		BillPayPage firstPayment = fillCompleteBillPayForm(overview, data, sourceAccount, true).submitPayment();
		assertNoBackendError(firstPayment);
		Assert.assertTrue(firstPayment.isPaymentSuccessful(), "First Bill Payment did not complete.");
		pass("Step 2 Passed: First payment completed for " + formatCurrency(data.get("Amount")) + ".");

		info("Step 3: Return to Accounts Overview and submit a second Bill Payment with different data.");
		BillPayAccountOverviewPage afterFirstPayment = firstPayment.goToAccountsOverview();
		Map<String, String> secondPaymentData = new HashMap<>(data);
		secondPaymentData.put("PayeeName", data.get("SecondPayeeName"));
		secondPaymentData.put("Amount", data.get("SecondAmount"));

		BillPayPage secondPayment = fillCompleteBillPayForm(
				afterFirstPayment, secondPaymentData, sourceAccount, true).submitPayment();
		assertNoBackendError(secondPayment);
		Assert.assertTrue(secondPayment.isPaymentSuccessful(), "Second Bill Payment did not complete.");
		pass("Step 3 Passed: Second payment completed for " + formatCurrency(data.get("SecondAmount")) + ".");

		info("Step 4: Verify cumulative source-account balance deduction equals both payments.");
		BillPayAccountOverviewPage afterSecondPayment = secondPayment.goToAccountsOverview();
		double balanceAfter = afterSecondPayment.getCurrentBalance(sourceAccount);
		double expectedDeduction = Double.parseDouble(data.get("Amount"))
				+ Double.parseDouble(data.get("SecondAmount"));
		Assert.assertEquals(balanceAfter, balanceBefore - expectedDeduction, 0.01,
				"Source-account balance does not reflect the cumulative value of both Bill Payments.");
		pass("Step 4 Passed: Cumulative deduction = " + formatCurrency(expectedDeduction)
				+ ", final balance = " + formatCurrency(balanceAfter) + ".");

		info("Step 5: Verify both Bill Payment transactions are present in account history.");
		AccountDetailsPage details = afterSecondPayment.openAccount(sourceAccount);
		Assert.assertTrue(details.hasBillPaymentTransaction(data.get("PayeeName"), data.get("Amount")),
				"First Bill Pay transaction was not found in account history.");
		Assert.assertTrue(details.hasBillPaymentTransaction(data.get("SecondPayeeName"), data.get("SecondAmount")),
				"Second Bill Pay transaction was not found in account history.");
		pass("Verification Passed: Both Bill Payments are persisted and cumulative balance impact is correct.");
	}

	@Test(groups = { "system", "e2e", "regression" },
			description = "PB_AUTO_10 - Verify Bill Payment persists after logout and re-login")
	public void pbAuto10_verifyBillPaymentPersistenceAcrossUserSession() {
		Map<String, String> data = data("PB_AUTO_10");

		info("Step 1: Login and create a successful Bill Payment.");
		BillPayAccountOverviewPage overview = loginAndGetOverview();
		String sourceAccount = overview.getFirstAccountId();
		BillPayPage billPay = fillCompleteBillPayForm(overview, data, sourceAccount, true).submitPayment();
		assertNoBackendError(billPay);
		Assert.assertTrue(billPay.isPaymentSuccessful(), "Bill Payment did not complete before logout.");
		pass("Step 1 Passed: Bill Payment completed for Payee=" + data.get("PayeeName")
				+ ", Amount=" + formatCurrency(data.get("Amount")) + ".");

		info("Step 2: Return to Accounts Overview and log out.");
		BillPayAccountOverviewPage afterPayment = billPay.goToAccountsOverview();
		afterPayment.logout();
		pass("Step 2 Passed: User logged out successfully.");

		info("Step 3: Log in again with the same valid credentials.");
		BillPayAccountOverviewPage reloggedOverview = loginAndGetOverview();
		Assert.assertTrue(reloggedOverview.getAllAccountIds().contains(sourceAccount),
				"Original source account is not available after re-login.");
		pass("Step 3 Passed: Re-login successful and source account is still available.");

		info("Step 4: Open the original source account after the new session.");
		AccountDetailsPage details = reloggedOverview.openAccount(sourceAccount);
		Assert.assertTrue(details.isTransactionTableDisplayed(),
				"Transaction history is not available after re-login.");
		pass("Step 4 Passed: Account Activity is available in the new session.");

		info("Step 5: Verify the Bill Payment created before logout still exists.");
		Assert.assertTrue(details.hasBillPaymentTransaction(data.get("PayeeName"), data.get("Amount")),
				"Bill Pay transaction did not persist across logout and re-login.");
		pass("Verification Passed: Bill Payment persists across user sessions.");
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
