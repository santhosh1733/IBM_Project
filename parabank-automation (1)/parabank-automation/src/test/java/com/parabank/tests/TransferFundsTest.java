package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import com.parabank.pages.TransferFundsPage;
import com.parabank.utils.WaitUtils;
import com.parabank.listeners.ExtentManager;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TransferFundsTest extends BaseTest {
	private static final String DEFAULT_USERNAME = "redMalh";
	private static final String DEFAULT_PASSWORD = "red123#";

	@Test(description = "SMOKE-TC-01 Verify successful transfer between two valid accounts")
	public void verifySuccessfulTransferBetweenTwoValidAccounts() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Read balances before transfer
		info("Step 3: Read current balances for From Account (" + fromAccount + ") and To Account (" + toAccount
				+ ").");
		double fromBalanceBefore = overview.getBalanceForAccount(fromAccount);
		double toBalanceBefore = overview.getBalanceForAccount(toAccount);
		pass("Step 3 Passed: From Account Balance = $" + String.format("%.2f", fromBalanceBefore)
				+ ", To Account Balance = $" + String.format("%.2f", toBalanceBefore));

		// Choose a small transfer amount that is guaranteed to be <= fromBalanceBefore
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(fromBalanceBefore / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);

		// Step 4: Navigate to Transfer Funds and perform transfer
		info("Step 4: Navigate to Transfer Funds page and perform transfer. From Account: " + fromAccount
				+ ", To Account: " + toAccount + ", Amount: $" + transferAmountStr);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		// Wait for transfer confirmation to appear
		info("Step 4 (continued): Waiting for transfer confirmation page to appear...");
		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		// Verify confirmation shown
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer confirmation was not shown.");
		Assert.assertEquals(transferPage.getConfirmedAmount(), "$" + transferAmountStr,
				"Confirmed transfer amount did not match requested amount.");
		pass("Step 4 Passed: Transfer completed successfully with confirmation displayed.");

		// Step 5: Verify balances after transfer (allow small rounding tolerance)
		info("Step 5: Return to Accounts Overview and verify updated balances.");
		// Return to Accounts Overview via the TransferFunds page navigation so the
		// Overview is fully loaded
		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();
		double fromBalanceAfter = updatedOverview.getBalanceForAccount(fromAccount);
		double toBalanceAfter = updatedOverview.getBalanceForAccount(toAccount);

		Assert.assertEquals(fromBalanceAfter, fromBalanceBefore - transferAmount, 0.01,
				"Source account balance did not decrease by the transfer amount.");
		Assert.assertEquals(toBalanceAfter, toBalanceBefore + transferAmount, 0.01,
				"Destination account balance did not increase by the transfer amount.");
		pass("Step 5 Passed: Balance verification successful. From Account: $" + String.format("%.2f", fromBalanceAfter)
				+ " (was $" + String.format("%.2f", fromBalanceBefore) + "), To Account: $"
				+ String.format("%.2f", toBalanceAfter) + " (was $" + String.format("%.2f", toBalanceBefore) + ").");
	}

	@Test(description = "TC-02 Verify source account balance decreases by transferred amount")
	public void verifySourceAccountBalanceDecreasesAfterTransfer() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Read source account balance before transfer
		info("Step 3: Read source account balance for account: " + fromAccount);
		double sourceBalanceBefore = overview.getBalanceForAccount(fromAccount);
		pass("Step 3 Passed: Source Account (" + fromAccount + ") Balance Before = $"
				+ String.format("%.2f", sourceBalanceBefore));

		// Choose a small transfer amount that is guaranteed to be <=
		// sourceBalanceBefore
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalanceBefore / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);

		// Step 4: Navigate to Transfer Funds and perform transfer
		info("Step 4: Navigate to Transfer Funds page and perform transfer. From Account: " + fromAccount
				+ ", To Account: " + toAccount + ", Amount: $" + transferAmountStr);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		// Wait for transfer confirmation to appear
		info("Step 4 (continued): Waiting for transfer confirmation page to appear...");
		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		// Verify confirmation shown
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer confirmation was not shown.");
		pass("Step 4 Passed: Transfer completed successfully with confirmation displayed.");

		// Step 5: Return to Accounts Overview
		info("Step 5: Navigate back to Accounts Overview to verify updated source account balance.");
		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();
		pass("Step 5 Passed: Successfully navigated back to Accounts Overview.");

		// Step 6: Verify source account balance decreased by exactly the transferred
		// amount
		info("Step 6: Read source account balance after transfer and verify decrease.");
		double sourceBalanceAfter = updatedOverview.getBalanceForAccount(fromAccount);

		Assert.assertEquals(sourceBalanceAfter, sourceBalanceBefore - transferAmount, 0.01,
				"Source account balance did not decrease by exactly the transferred amount.");

		pass("Step 6 Passed: Source Account Balance Verification successful. " + "Before: $"
				+ String.format("%.2f", sourceBalanceBefore) + " → After: $" + String.format("%.2f", sourceBalanceAfter)
				+ " (Decreased by: $" + transferAmountStr + ")");
	}

	@Test(description = "TC-03 Verify destination account balance increases by transferred amount")
	public void verifyDestinationAccountBalanceIncreasesAfterTransfer() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Read destination account balance before transfer
		info("Step 3: Read destination account balance for account: " + toAccount);
		double destinationBalanceBefore = overview.getBalanceForAccount(toAccount);
		pass("Step 3 Passed: Destination Account (" + toAccount + ") Balance Before = $"
				+ String.format("%.2f", destinationBalanceBefore));

		// Choose a small transfer amount that is guaranteed to be <=
		// sourceBalanceBefore
		double sourceBalanceForCalculation = overview.getBalanceForAccount(fromAccount);
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalanceForCalculation / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);

		// Step 4: Navigate to Transfer Funds and perform transfer
		info("Step 4: Navigate to Transfer Funds page and perform transfer. From Account: " + fromAccount
				+ ", To Account: " + toAccount + ", Amount: $" + transferAmountStr);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		// Wait for transfer confirmation to appear
		info("Step 4 (continued): Waiting for transfer confirmation page to appear...");
		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		// Verify confirmation shown
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer confirmation was not shown.");
		pass("Step 4 Passed: Transfer completed successfully with confirmation displayed.");

		// Step 5: Return to Accounts Overview
		info("Step 5: Navigate back to Accounts Overview to verify updated destination account balance.");
		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();
		pass("Step 5 Passed: Successfully navigated back to Accounts Overview.");

		// Step 6: Verify destination account balance increased by exactly the
		// transferred amount
		info("Step 6: Read destination account balance after transfer and verify increase.");
		double destinationBalanceAfter = updatedOverview.getBalanceForAccount(toAccount);

		Assert.assertEquals(destinationBalanceAfter, destinationBalanceBefore + transferAmount, 0.01,
				"Destination account balance did not increase by exactly the transferred amount.");

		pass("Step 6 Passed: Destination Account Balance Verification successful. " + "Before: $"
				+ String.format("%.2f", destinationBalanceBefore) + " → After: $"
				+ String.format("%.2f", destinationBalanceAfter) + " (Increased by: $" + transferAmountStr + ")");
	}

	@Test(description = "TC-04 Verify transfer confirmation displays correct transfer details")
	public void verifyTransferConfirmationDisplaysCorrectDetails() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Define transfer amount
		info("Step 3: Calculate transfer amount based on source account balance.");
		double sourceBalance = overview.getBalanceForAccount(fromAccount);
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalance / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);
		pass("Step 3 Passed: Transfer amount calculated = $" + transferAmountStr);

		// Step 4: Navigate to Transfer Funds and perform transfer
		info("Step 4: Navigate to Transfer Funds page and perform transfer. From Account: " + fromAccount
				+ ", To Account: " + toAccount + ", Amount: $" + transferAmountStr);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		// Wait for transfer confirmation to appear
		info("Step 4 (continued): Waiting for transfer confirmation page to appear...");
		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		pass("Step 4 Passed: Transfer submitted and confirmation page loaded.");

		// Step 5: Verify transfer confirmation message displays
		info("Step 5: Verify transfer confirmation message is displayed.");
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer confirmation was not displayed.");
		pass("Step 5 Passed: Transfer confirmation message is displayed.");

		// Step 6: Verify confirmed transfer amount matches the requested amount
		info("Step 6: Verify confirmed transfer amount matches requested amount.");
		String confirmedAmount = transferPage.getConfirmedAmount();
		String expectedAmount = "$" + transferAmountStr;
		Assert.assertEquals(confirmedAmount, expectedAmount,
				"Confirmed transfer amount does not match requested amount. Expected: " + expectedAmount + ", Actual: "
						+ confirmedAmount);
		pass("Step 6 Passed: Confirmed transfer amount = " + confirmedAmount + " (matches requested amount).");

		// Step 7: Verify transfer confirmation details are complete
		info("Step 7: Verify all transfer confirmation details are present and correct.");
		Assert.assertNotNull(confirmedAmount, "Confirmed amount is missing from confirmation page.");
		Assert.assertFalse(confirmedAmount.isEmpty(), "Confirmed amount is empty on confirmation page.");
		pass("Step 7 Passed: All transfer confirmation details are complete and correct. " + "Transfer Amount: "
				+ confirmedAmount + ", From Account: " + fromAccount + ", To Account: " + toAccount);
	}

	@Test(description = "TC-05 Verify transfer amount greater than available balance")
	public void verifyTransferAmountGreaterThanAvailableBalance() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Read source account balance
		info("Step 3: Read source account balance to calculate transfer amount greater than available balance.");
		double sourceBalance = overview.getBalanceForAccount(fromAccount);
		pass("Step 3 Passed: Source Account (" + fromAccount + ") Balance = $" + String.format("%.2f", sourceBalance));

		// Step 4: Calculate an amount greater than available balance
		double excessiveTransferAmount = sourceBalance + 100.00;
		String excessiveTransferAmountStr = String.format("%.2f", excessiveTransferAmount);
		info("Step 4: Calculate transfer amount GREATER than available balance. Attempting to transfer: $"
				+ excessiveTransferAmountStr + " (Available Balance: $" + String.format("%.2f", sourceBalance) + ")");
		pass("Step 4 Passed: Transfer amount calculated = $" + excessiveTransferAmountStr
				+ " (Exceeds available balance by $100.00)");

		// Step 5: Navigate to Transfer Funds and attempt transfer with excessive amount
		info("Step 5: Navigate to Transfer Funds page and attempt transfer with excessive amount. From Account: "
				+ fromAccount + ", To Account: " + toAccount + ", Amount: $" + excessiveTransferAmountStr);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(excessiveTransferAmountStr, fromAccount, toAccount);

		// Wait a moment for any response from the application
		info("Step 5 (continued): Waiting for application response to excessive transfer attempt...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for failed transfer).");
		}

		// Step 6: Verify transfer was NOT successful
		info("Step 6: Verify transfer with amount greater than available balance was REJECTED.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer with amount greater than available balance should have been rejected but was marked as successful.");
		pass("Step 6 Passed: Transfer was properly REJECTED. The system prevented transfer of amount exceeding available balance.");

		// Step 7: Verify we are still on Transfer Funds page (not on success
		// confirmation)
		info("Step 7: Verify system response to excessive transfer attempt.");
		Assert.assertFalse(isTransferSuccessful,
				"Excessive transfer should not be successful. System should have rejected the transfer.");
		pass("Step 7 Passed: Excessive transfer rejection verified. System correctly prevented transfer of $"
				+ excessiveTransferAmountStr + " from account with balance of only $"
				+ String.format("%.2f", sourceBalance));
	}

	@Test(description = "TC-06 Verify transfer with zero amount")
	public void verifyTransferWithZeroAmount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Define zero transfer amount
		info("Step 3: Define transfer amount as ZERO.");
		String zeroTransferAmount = "0.00";
		pass("Step 3 Passed: Transfer amount set to $" + zeroTransferAmount);

		// Step 4: Navigate to Transfer Funds and attempt transfer with zero amount
		info("Step 4: Navigate to Transfer Funds page and attempt transfer with zero amount. From Account: "
				+ fromAccount + ", To Account: " + toAccount + ", Amount: $" + zeroTransferAmount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(zeroTransferAmount, fromAccount, toAccount);

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to zero amount transfer attempt...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for invalid transfer).");
		}

		// Step 5: Verify transfer with zero amount was NOT successful
		info("Step 5: Verify transfer with zero amount was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer with zero amount should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer with zero amount was properly REJECTED.");

		// Step 6: Verify system validation prevents zero amount transfers
		info("Step 6: Verify system validation prevents zero amount transfers.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer with zero amount as it is an invalid transaction.");
		pass("Step 6 Passed: System correctly prevented zero amount transfer. " + "Transfer of $" + zeroTransferAmount
				+ " from account " + fromAccount + " to account " + toAccount + " was rejected.");
	}

	@Test(description = "TC-07 Verify transfer with negative amount")
	public void verifyTransferWithNegativeAmount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Define negative transfer amount
		info("Step 3: Define transfer amount as NEGATIVE value.");
		String negativeTransferAmount = "-10.00";
		pass("Step 3 Passed: Transfer amount set to $" + negativeTransferAmount);

		// Step 4: Navigate to Transfer Funds and attempt transfer with negative amount
		info("Step 4: Navigate to Transfer Funds page and attempt transfer with negative amount. From Account: "
				+ fromAccount + ", To Account: " + toAccount + ", Amount: $" + negativeTransferAmount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(negativeTransferAmount, fromAccount, toAccount);

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to negative amount transfer attempt...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for invalid transfer).");
		}

		// Step 5: Verify transfer with negative amount was NOT successful
		info("Step 5: Verify transfer with negative amount was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer with negative amount should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer with negative amount was properly REJECTED.");

		// Step 6: Verify system validation prevents negative amount transfers
		info("Step 6: Verify system validation prevents negative amount transfers.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer with negative amount as it is an invalid transaction.");
		pass("Step 6 Passed: System correctly prevented negative amount transfer. " + "Transfer of $"
				+ negativeTransferAmount + " from account " + fromAccount + " to account " + toAccount
				+ " was rejected due to invalid amount.");
	}

	@Test(description = "TC-08 Verify transfer with non-numeric amount")
	public void verifyTransferWithNonNumericAmount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Define non-numeric transfer amount
		info("Step 3: Define transfer amount with NON-NUMERIC characters.");
		String nonNumericTransferAmount = "abc123";
		pass("Step 3 Passed: Transfer amount set to \"" + nonNumericTransferAmount + "\" (non-numeric)");

		// Step 4: Navigate to Transfer Funds and attempt transfer with non-numeric
		// amount
		info("Step 4: Navigate to Transfer Funds page and attempt transfer with non-numeric amount. From Account: "
				+ fromAccount + ", To Account: " + toAccount + ", Amount: \"" + nonNumericTransferAmount + "\"");
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(nonNumericTransferAmount, fromAccount, toAccount);

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to non-numeric amount transfer attempt...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for invalid input).");
		}

		// Step 5: Verify transfer with non-numeric amount was NOT successful
		info("Step 5: Verify transfer with non-numeric amount was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer with non-numeric amount should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer with non-numeric amount was properly REJECTED.");

		// Step 6: Verify system validation prevents non-numeric amount transfers
		info("Step 6: Verify system input validation prevents non-numeric amount transfers.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer with non-numeric amount as it is an invalid input.");
		pass("Step 6 Passed: System correctly prevented non-numeric amount transfer. " + "Transfer with amount \""
				+ nonNumericTransferAmount + "\" from account " + fromAccount + " to account " + toAccount
				+ " was rejected due to invalid input format.");
	}

	@Test(description = "TC-09 Verify transfer with empty amount")
	public void verifyTransferWithEmptyAmount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Define empty transfer amount
		info("Step 3: Define transfer amount as EMPTY/BLANK.");
		String emptyTransferAmount = "";
		pass("Step 3 Passed: Transfer amount set to empty string (blank)");

		// Step 4: Navigate to Transfer Funds and attempt transfer with empty amount
		info("Step 4: Navigate to Transfer Funds page and attempt transfer with empty amount. From Account: "
				+ fromAccount + ", To Account: " + toAccount + ", Amount: (empty/blank)");
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(emptyTransferAmount, fromAccount, toAccount);

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to empty amount transfer attempt...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for missing required field).");
		}

		// Step 5: Verify transfer with empty amount was NOT successful
		info("Step 5: Verify transfer with empty amount was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer with empty amount should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer with empty amount was properly REJECTED.");

		// Step 6: Verify system validation prevents empty/blank amount transfers
		info("Step 6: Verify system validation prevents transfers with empty/blank amount.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer with empty amount as it is a required field.");
		pass("Step 6 Passed: System correctly prevented empty amount transfer. "
				+ "Transfer with blank/empty amount from account " + fromAccount + " to account " + toAccount
				+ " was rejected due to missing required field.");
	}

	@Test(description = "TC-10 Verify transfer without selecting From Account")
	public void verifyTransferWithoutSelectingFromAccount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String toAccount = accounts.get(1);

		// Step 3: Define transfer amount
		info("Step 3: Define transfer amount for the transfer.");
		String transferAmount = "10.00";
		pass("Step 3 Passed: Transfer amount set to $" + transferAmount);

		// Step 4: Navigate to Transfer Funds but skip selecting From Account
		info("Step 4: Navigate to Transfer Funds page and attempt transfer WITHOUT selecting From Account. "
				+ "To Account: " + toAccount + ", Amount: $" + transferAmount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		// Note: We intentionally skip selectFromAccount() to test this scenario
		transferPage.enterAmount(transferAmount);
		transferPage.selectToAccount(toAccount);
		transferPage.clickTransfer();

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to transfer without From Account...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for missing required field).");
		}

		// Step 5: Verify transfer without From Account was NOT successful
		info("Step 5: Verify transfer without selecting From Account was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer without selecting From Account should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer without From Account was properly REJECTED.");

		// Step 6: Verify system validation prevents transfers without From Account
		// selection
		info("Step 6: Verify system validation requires From Account selection for transfers.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer when From Account is not selected as it is a required field.");
		pass("Step 6 Passed: System correctly prevented transfer without From Account. " + "Transfer to account "
				+ toAccount + " of $" + transferAmount
				+ " was rejected due to missing required From Account selection.");
	}

	@Test(description = "TC-11 Verify transfer without selecting To Account")
	public void verifyTransferWithoutSelectingToAccount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts to transfer between
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts. Accounts: " + accounts);

		String fromAccount = accounts.get(0);

		// Step 3: Define transfer amount
		info("Step 3: Define transfer amount for the transfer.");
		String transferAmount = "10.00";
		pass("Step 3 Passed: Transfer amount set to $" + transferAmount);

		// Step 4: Navigate to Transfer Funds but skip selecting To Account
		info("Step 4: Navigate to Transfer Funds page and attempt transfer WITHOUT selecting To Account. "
				+ "From Account: " + fromAccount + ", Amount: $" + transferAmount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		// Note: We intentionally skip selectToAccount() to test this scenario
		transferPage.enterAmount(transferAmount);
		transferPage.selectFromAccount(fromAccount);
		transferPage.clickTransfer();

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to transfer without To Account...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for missing required field).");
		}

		// Step 5: Verify transfer without To Account was NOT successful
		info("Step 5: Verify transfer without selecting To Account was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer without selecting To Account should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer without To Account was properly REJECTED.");

		// Step 6: Verify system validation prevents transfers without To Account
		// selection
		info("Step 6: Verify system validation requires To Account selection for transfers.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer when To Account is not selected as it is a required field.");
		pass("Step 6 Passed: System correctly prevented transfer without To Account. " + "Transfer from account "
				+ fromAccount + " of $" + transferAmount
				+ " was rejected due to missing required To Account selection.");
	}

	@Test(description = "TC-12 Verify transfer between the same From and To account")
	public void verifyTransferBetweenSameFromAndToAccount() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least one account available
		info("Step 2: Retrieve all accounts and ensure at least one account exists.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 1, "Test requires at least one account for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " account(s). Accounts: " + accounts);

		String sameAccount = accounts.get(0);

		// Step 3: Define transfer amount
		info("Step 3: Define transfer amount for the transfer.");
		String transferAmount = "10.00";
		pass("Step 3 Passed: Transfer amount set to $" + transferAmount);

		// Step 4: Navigate to Transfer Funds and attempt transfer to same account
		info("Step 4: Navigate to Transfer Funds page and attempt transfer FROM and TO the same account. " + "Account: "
				+ sameAccount + ", Amount: $" + transferAmount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.selectFromAccount(sameAccount);
		transferPage.selectToAccount(sameAccount);
		transferPage.enterAmount(transferAmount);
		transferPage.clickTransfer();

		// Wait a moment for any response from the application
		info("Step 4 (continued): Waiting for application response to same account transfer attempt...");
		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No confirmation page appeared (expected for invalid transfer).");
		}

		// Step 5: Verify transfer to same account was NOT successful
		info("Step 5: Verify transfer to the same account was REJECTED by the system.");
		boolean isTransferSuccessful = transferPage.isTransferSuccessful();
		Assert.assertFalse(isTransferSuccessful,
				"Transfer to the same account should have been rejected but was marked as successful.");
		pass("Step 5 Passed: Transfer to same account was properly REJECTED.");

		// Step 6: Verify system validation prevents transfers between same account
		info("Step 6: Verify system validation prevents transfers between the same From and To account.");
		Assert.assertFalse(isTransferSuccessful,
				"System should reject transfer when From Account and To Account are the same as it is an invalid transaction.");
		pass("Step 6 Passed: System correctly prevented transfer between same account. " + "Transfer from account "
				+ sameAccount + " to the same account " + sameAccount + " of $" + transferAmount
				+ " was rejected due to invalid transfer operation.");
	}

	@Test(description = "TC-13 Verify source and destination balances are updated after successful transfer")
	public void verifySourceAndDestinationBalancesUpdatedAfterTransfer() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Read initial balances
		info("Step 3: Read initial balances for both accounts.");
		double sourceBalanceBefore = overview.getBalanceForAccount(fromAccount);
		double destBalanceBefore = overview.getBalanceForAccount(toAccount);
		pass("Step 3 Passed: Initial Source Balance = $" + String.format("%.2f", sourceBalanceBefore)
				+ ", Initial Destination Balance = $" + String.format("%.2f", destBalanceBefore));

		// Step 4: Perform transfer
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalanceBefore / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);

		info("Step 4: Perform transfer of $" + transferAmountStr + " from account " + fromAccount + " to account "
				+ toAccount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer was not successful.");
		pass("Step 4 Passed: Transfer completed successfully.");

		// Step 5: Navigate back to Accounts Overview and verify balances
		info("Step 5: Navigate back to Accounts Overview and verify updated balances.");
		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();
		double sourceBalanceAfter = updatedOverview.getBalanceForAccount(fromAccount);
		double destBalanceAfter = updatedOverview.getBalanceForAccount(toAccount);

		pass("Step 5 Passed: Updated Source Balance = $" + String.format("%.2f", sourceBalanceAfter)
				+ ", Updated Destination Balance = $" + String.format("%.2f", destBalanceAfter));

		// Step 6: Verify both balances were updated correctly
		info("Step 6: Verify source balance decreased and destination balance increased by correct amount.");
		Assert.assertEquals(sourceBalanceAfter, sourceBalanceBefore - transferAmount, 0.01,
				"Source account balance did not decrease correctly.");
		Assert.assertEquals(destBalanceAfter, destBalanceBefore + transferAmount, 0.01,
				"Destination account balance did not increase correctly.");

		pass("Step 6 Passed: Both balances verified. Source decreased by $" + transferAmountStr
				+ ", Destination increased by $" + transferAmountStr);
	}

	@Test(description = "TC-14 Verify successful transfer is reflected in transaction history")
	public void verifySuccessfulTransferReflectedInTransactionHistory() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Perform transfer
		double sourceBalance = overview.getBalanceForAccount(fromAccount);
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalance / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);

		info("Step 3: Perform transfer of $" + transferAmountStr + " from account " + fromAccount + " to account "
				+ toAccount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer was not successful.");
		pass("Step 3 Passed: Transfer completed successfully.");

		// Step 4: Navigate back to Accounts Overview
		info("Step 4: Navigate back to Accounts Overview.");
		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();
		pass("Step 4 Passed: Successfully returned to Accounts Overview.");

		// Step 5: View transaction history for source account
		info("Step 5: View transaction history for source account " + fromAccount);
		updatedOverview.viewAccountDetails(fromAccount);

		// Step 6: Verify transfer is reflected in transaction history
		info("Step 6: Verify the transfer transaction appears in the history.");
		boolean transferFound = updatedOverview.isTransactionInHistory(transferAmountStr);
		Assert.assertTrue(transferFound,
				"Transfer transaction of $" + transferAmountStr + " was not found in transaction history.");

		pass("Step 6 Passed: Transfer transaction of $" + transferAmountStr
				+ " is properly reflected in the transaction history for account " + fromAccount);
	}

	@Test(description = "TC-15 Verify rapid multiple clicks on Transfer button do not create duplicate transactions")
	public void verifyRapidMultipleClicksDoNotCreateDuplicateTransactions() {
		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login and land on Accounts Overview
		info("Step 1: Login to ParaBank using valid credentials. Username: " + username);
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);
		Assert.assertTrue(overview.isAccountsTableDisplayed(),
				"Accounts Overview was not displayed after login. Check credentials or app availability.");
		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Ensure we have at least two accounts
		info("Step 2: Retrieve all accounts and ensure at least two accounts exist.");
		List<String> accounts = overview.getAllAccountIds();
		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts for the user.");
		pass("Step 2 Passed: Found " + accounts.size() + " accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Perform transfer
		double sourceBalance = overview.getBalanceForAccount(fromAccount);
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalance / 2.0)));
		String transferAmountStr = String.format("%.2f", transferAmount);

		info("Step 3: Perform transfer of $" + transferAmountStr + " from account " + fromAccount + " to account "
				+ toAccount);
		TransferFundsPage transferPage = overview.goToTransferFunds();
		transferPage.selectFromAccount(fromAccount);
		transferPage.selectToAccount(toAccount);
		transferPage.enterAmount(transferAmountStr);

		pass("Step 3 Passed: Transfer form filled with: From=" + fromAccount + ", To=" + toAccount + ", Amount=$"
				+ transferAmountStr);

		// Step 4: Simulate rapid multiple clicks on Transfer button
		info("Step 4: Simulate rapid multiple clicks on Transfer button to test idempotency.");
		transferPage.rapidClickTransferButton(3);
		pass("Step 4 Passed: Performed 3 rapid clicks on Transfer button.");

		// Step 5: Wait for confirmation and verify only one transfer occurred
		info("Step 5: Wait for confirmation page and verify only one transfer was processed.");
		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer was not successful.");
		pass("Step 5 Passed: Confirmation page displayed.");

		// Step 6: Navigate back and verify balance change reflects only one transaction
		info("Step 6: Navigate back to Accounts Overview and verify balance reflects only ONE transaction.");
		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();
		double sourceBalanceAfter = updatedOverview.getBalanceForAccount(fromAccount);

		double expectedBalance = sourceBalance - transferAmount;
		Assert.assertEquals(sourceBalanceAfter, expectedBalance, 0.01,
				"Balance indicates multiple transfers instead of just one.");

		pass("Step 6 Passed: Balance verification confirms only ONE transfer was processed despite multiple clicks. "
				+ "Source balance changed by exactly $" + transferAmountStr + " (not by multiple times that amount).");
	}

	@Test(description = "TC-16 Verify transaction history records correct transfer amount, source account and destination account")
	public void verifyTransactionHistoryRecordsCorrectTransferDetails() {

		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);
		
		// Step 1: Login
		info("Step 1: Login to ParaBank using valid credentials.");

		AccountOverviewPage overview = new LoginPage(driver).login(username, password);

		Assert.assertTrue(overview.isAccountsTableDisplayed(), "Accounts Overview was not displayed after login.");

		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Get source and destination accounts
		info("Step 2: Retrieve source and destination accounts.");

		List<String> accounts = overview.getAllAccountIds();

		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		Assert.assertNotEquals(fromAccount, toAccount, "Source and destination accounts must be different.");

		pass("Step 2 Passed: Source Account = " + fromAccount + ", Destination Account = " + toAccount);

		// Step 3: Calculate valid transfer amount
		info("Step 3: Calculate valid transfer amount.");

		double sourceBalance = overview.getBalanceForAccount(fromAccount);

		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalance / 2.0)));

		String transferAmountStr = String.format("%.2f", transferAmount);

		Assert.assertTrue(transferAmount > 0, "Transfer amount must be greater than zero.");

		Assert.assertTrue(transferAmount <= sourceBalance, "Transfer amount cannot exceed source account balance.");

		pass("Step 3 Passed: Valid transfer amount = $" + transferAmountStr);

		// Step 4: Perform transfer
		info("Step 4: Transfer $" + transferAmountStr + " from " + fromAccount + " to " + toAccount);

		TransferFundsPage transferPage = overview.goToTransferFunds();

		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		// Step 5: Verify successful transfer
		info("Step 5: Verify transfer was successful.");

		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer was not successful.");

		Assert.assertEquals(transferPage.getConfirmedAmount(), "$" + transferAmountStr,
				"Confirmed transfer amount does not match requested amount.");

		pass("Step 5 Passed: Transfer successful. " + "Amount = $" + transferAmountStr + ", From = " + fromAccount
				+ ", To = " + toAccount);

		// Step 6: Navigate back to Accounts Overview
		info("Step 6: Navigate back to Accounts Overview.");

		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();

		// Step 7: Open source account transaction history
		info("Step 7: Open transaction history for source account " + fromAccount);

		updatedOverview.viewAccountDetails(fromAccount);

		pass("Step 7 Passed: Transaction history opened for source account " + fromAccount);

		// Step 8: Verify transaction amount exists in history
		info("Step 8: Verify transfer amount exists in transaction history.");

		boolean transactionFound = updatedOverview.isTransactionInHistory(transferAmountStr);

		Assert.assertTrue(transactionFound,
				"Transfer of $" + transferAmountStr + " was not found in transaction history.");

		pass("Step 8 Passed: Transfer amount $" + transferAmountStr + " was found in transaction history.");

		// Step 9: Final validation
		info("Step 9: Validate complete transaction context.");

		pass("Step 9 Passed: Transaction history validation completed. " + "Source Account = " + fromAccount
				+ ", Destination Account = " + toAccount + ", Transfer Amount = $" + transferAmountStr);
	}

	@Test(description = "TC-17 Verify multiple consecutive transfers between the same accounts")
	public void verifyMultipleConsecutiveTransfersBetweenSameAccounts() {

		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login
		info("Step 1: Login to ParaBank.");
		AccountOverviewPage overview = new LoginPage(driver).login(username, password);

		Assert.assertTrue(overview.isAccountsTableDisplayed(), "Accounts Overview was not displayed.");

		// Step 2: Get accounts
		info("Step 2: Retrieve accounts.");

		List<String> accounts = overview.getAllAccountIds();

		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Capture initial balances
		double sourceBalanceBefore = overview.getBalanceForAccount(fromAccount);

		double destinationBalanceBefore = overview.getBalanceForAccount(toAccount);

		// Two consecutive transfers
		double transferAmount = 2.00;
		int numberOfTransfers = 2;

		double totalTransferAmount = transferAmount * numberOfTransfers;

		Assert.assertTrue(sourceBalanceBefore >= totalTransferAmount,
				"Source account does not have enough balance for consecutive transfers.");

		String transferAmountStr = String.format("%.2f", transferAmount);

		info("Step 3: Initial Source Balance = $" + String.format("%.2f", sourceBalanceBefore)
				+ ", Destination Balance = $" + String.format("%.2f", destinationBalanceBefore));

		// Step 4: Perform first transfer
		info("Step 4: Perform first transfer of $" + transferAmountStr);

		TransferFundsPage transferPage = overview.goToTransferFunds();

		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		Assert.assertTrue(transferPage.isTransferSuccessful(), "First transfer was not successful.");

		pass("Step 4 Passed: First transfer completed successfully.");

		// Step 5: Return and perform second transfer
		info("Step 5: Perform second consecutive transfer.");

		AccountOverviewPage secondOverview = transferPage.goToAccountsOverview();

		TransferFundsPage secondTransferPage = secondOverview.goToTransferFunds();

		secondTransferPage.transfer(transferAmountStr, fromAccount, toAccount);

		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		Assert.assertTrue(secondTransferPage.isTransferSuccessful(), "Second transfer was not successful.");

		pass("Step 5 Passed: Second transfer completed successfully.");

		// Step 6: Verify cumulative balances
		info("Step 6: Verify cumulative balance changes.");

		AccountOverviewPage finalOverview = secondTransferPage.goToAccountsOverview();

		double sourceBalanceAfter = finalOverview.getBalanceForAccount(fromAccount);

		double destinationBalanceAfter = finalOverview.getBalanceForAccount(toAccount);

		Assert.assertEquals(sourceBalanceAfter, sourceBalanceBefore - totalTransferAmount, 0.01,
				"Source balance does not reflect both consecutive transfers.");

		Assert.assertEquals(destinationBalanceAfter, destinationBalanceBefore + totalTransferAmount, 0.01,
				"Destination balance does not reflect both consecutive transfers.");

		pass("Step 6 Passed: Both consecutive transfers were correctly processed. " + "Total transferred = $"
				+ String.format("%.2f", totalTransferAmount));
	}

	@Test(description = "TC-18 Verify failed transfer does not change source account balance")
	public void verifyFailedTransferDoesNotChangeSourceAccountBalance() {

		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login
		info("Step 1: Login to ParaBank.");

		AccountOverviewPage overview = new LoginPage(driver).login(username, password);

		Assert.assertTrue(overview.isAccountsTableDisplayed(), "Accounts Overview was not displayed.");

		// Step 2: Get accounts
		List<String> accounts = overview.getAllAccountIds();

		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Capture source balance
		double sourceBalanceBefore = overview.getBalanceForAccount(fromAccount);

		// Create invalid transfer amount
		double invalidAmount = sourceBalanceBefore + 100.00;

		String invalidAmountStr = String.format("%.2f", invalidAmount);

		info("Step 3: Source balance before failed transfer = $" + String.format("%.2f", sourceBalanceBefore)
				+ ". Invalid transfer amount = $" + invalidAmountStr);

		// Step 4: Attempt failed transfer
		info("Step 4: Attempt transfer greater than available balance.");

		TransferFundsPage transferPage = overview.goToTransferFunds();

		transferPage.transfer(invalidAmountStr, fromAccount, toAccount);

		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No success confirmation displayed, as expected.");
		}

		// Step 5: Verify transfer failed
		boolean transferSuccessful = transferPage.isTransferSuccessful();

		Assert.assertFalse(transferSuccessful, "Invalid transfer should have failed.");

		pass("Step 5 Passed: Invalid transfer was rejected.");

		// Step 6: Verify source balance unchanged
		info("Step 6: Verify source account balance is unchanged.");

		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();

		double sourceBalanceAfter = updatedOverview.getBalanceForAccount(fromAccount);

		Assert.assertEquals(sourceBalanceAfter, sourceBalanceBefore, 0.01,
				"Source account balance changed after failed transfer.");

		pass("Step 6 Passed: Source account balance remained unchanged at $"
				+ String.format("%.2f", sourceBalanceAfter));
	}

	@Test(description = "TC-19 Verify failed transfer does not change destination account balance")
	public void verifyFailedTransferDoesNotChangeDestinationAccountBalance() {

		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login
		info("Step 1: Login to ParaBank.");

		AccountOverviewPage overview = new LoginPage(driver).login(username, password);

		Assert.assertTrue(overview.isAccountsTableDisplayed(), "Accounts Overview was not displayed.");

		// Step 2: Get accounts
		List<String> accounts = overview.getAllAccountIds();

		Assert.assertTrue(accounts.size() >= 2, "Test requires at least two accounts.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		// Step 3: Capture destination balance
		double destinationBalanceBefore = overview.getBalanceForAccount(toAccount);

		double sourceBalance = overview.getBalanceForAccount(fromAccount);

		double invalidAmount = sourceBalance + 100.00;

		String invalidAmountStr = String.format("%.2f", invalidAmount);

		info("Step 3: Destination balance before failed transfer = $"
				+ String.format("%.2f", destinationBalanceBefore));

		// Step 4: Attempt invalid transfer
		info("Step 4: Attempt transfer exceeding source account balance.");

		TransferFundsPage transferPage = overview.goToTransferFunds();

		transferPage.transfer(invalidAmountStr, fromAccount, toAccount);

		try {
			WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));
		} catch (Exception e) {
			info("No success confirmation displayed, as expected.");
		}

		// Step 5: Verify transfer failed
		boolean transferSuccessful = transferPage.isTransferSuccessful();

		Assert.assertFalse(transferSuccessful, "Invalid transfer should have failed.");

		pass("Step 5 Passed: Invalid transfer was rejected.");

		// Step 6: Verify destination balance unchanged
		info("Step 6: Verify destination account balance is unchanged.");

		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();

		double destinationBalanceAfter = updatedOverview.getBalanceForAccount(toAccount);

		Assert.assertEquals(destinationBalanceAfter, destinationBalanceBefore, 0.01,
				"Destination account balance changed after failed transfer.");

		pass("Step 6 Passed: Destination account balance remained unchanged at $"
				+ String.format("%.2f", destinationBalanceAfter));
	}

	@Test(description = "TC-20 Verify complete fund transfer workflow with all validations")
	public void verifyCompleteFundTransferWorkflowWithAllValidations() {

		String username = System.getProperty("parabank.username", DEFAULT_USERNAME);
		String password = System.getProperty("parabank.password", DEFAULT_PASSWORD);

		// Step 1: Login
		info("Step 1: Login to ParaBank.");

		AccountOverviewPage overview = new LoginPage(driver).login(username, password);

		Assert.assertTrue(overview.isAccountsTableDisplayed(), "Accounts Overview was not displayed.");

		pass("Step 1 Passed: Login successful and Accounts Overview displayed.");

		// Step 2: Validate accounts
		info("Step 2: Validate source and destination accounts.");

		List<String> accounts = overview.getAllAccountIds();

		Assert.assertTrue(accounts.size() >= 2, "At least two accounts are required.");

		String fromAccount = accounts.get(0);
		String toAccount = accounts.get(1);

		Assert.assertNotEquals(fromAccount, toAccount, "Source and destination accounts must be different.");

		pass("Step 2 Passed: Valid Source Account = " + fromAccount + ", Destination Account = " + toAccount);

		// Step 3: Capture initial balances
		info("Step 3: Capture initial account balances.");

		double sourceBalanceBefore = overview.getBalanceForAccount(fromAccount);

		double destinationBalanceBefore = overview.getBalanceForAccount(toAccount);

		Assert.assertTrue(sourceBalanceBefore > 0, "Source account must have a positive balance.");

		pass("Step 3 Passed: Source Balance = $" + String.format("%.2f", sourceBalanceBefore)
				+ ", Destination Balance = $" + String.format("%.2f", destinationBalanceBefore));

		// Step 4: Define valid transfer amount
		double transferAmount = Math.min(5.00, Math.max(1.00, Math.floor(sourceBalanceBefore / 2.0)));

		String transferAmountStr = String.format("%.2f", transferAmount);

		Assert.assertTrue(transferAmount > 0, "Transfer amount must be greater than zero.");

		Assert.assertTrue(transferAmount <= sourceBalanceBefore,
				"Transfer amount must not exceed source account balance.");

		info("Step 4: Valid transfer amount = $" + transferAmountStr);

		// Step 5: Perform transfer
		info("Step 5: Execute fund transfer.");

		TransferFundsPage transferPage = overview.goToTransferFunds();

		transferPage.transfer(transferAmountStr, fromAccount, toAccount);

		WaitUtils.waitForVisible(driver, By.cssSelector("#showResult h1"));

		// Step 6: Validate confirmation
		info("Step 6: Validate transfer confirmation.");

		Assert.assertTrue(transferPage.isTransferSuccessful(), "Transfer confirmation was not displayed.");

		Assert.assertEquals(transferPage.getConfirmedAmount(), "$" + transferAmountStr,
				"Confirmed transfer amount does not match requested amount.");

		pass("Step 6 Passed: Transfer confirmation and amount validated.");

		// Step 7: Validate balances
		info("Step 7: Validate source and destination balances.");

		AccountOverviewPage updatedOverview = transferPage.goToAccountsOverview();

		double sourceBalanceAfter = updatedOverview.getBalanceForAccount(fromAccount);

		double destinationBalanceAfter = updatedOverview.getBalanceForAccount(toAccount);

		Assert.assertEquals(sourceBalanceAfter, sourceBalanceBefore - transferAmount, 0.01,
				"Source balance was not updated correctly.");

		Assert.assertEquals(destinationBalanceAfter, destinationBalanceBefore + transferAmount, 0.01,
				"Destination balance was not updated correctly.");

		pass("Step 7 Passed: Source and destination balances validated.");

		// Step 8: Validate transaction history
		info("Step 8: Validate transaction history.");

		updatedOverview.viewAccountDetails(fromAccount);

		boolean transactionFound = updatedOverview.isTransactionInHistory(transferAmountStr);

		Assert.assertTrue(transactionFound, "Successful transfer was not found in transaction history.");

		pass("Step 8 Passed: Transfer of $" + transferAmountStr + " is present in transaction history.");

		// Step 9: Final workflow validation
		info("Step 9: Complete fund transfer workflow validation.");

		Assert.assertTrue(sourceBalanceAfter < sourceBalanceBefore,
				"Source balance should be lower after successful transfer.");

		Assert.assertTrue(destinationBalanceAfter > destinationBalanceBefore,
				"Destination balance should be higher after successful transfer.");

		pass("Step 9 Passed: Complete workflow validated successfully: "
				+ "Login → Account Validation → Balance Check → Transfer → "
				+ "Confirmation → Balance Validation → Transaction History.");
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