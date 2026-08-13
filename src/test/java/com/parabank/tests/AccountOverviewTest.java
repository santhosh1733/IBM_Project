package com.parabank.tests;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import com.parabank.pages.TransferFundsPage;

public class AccountOverviewTest extends BaseTest {
	@Test
	public void verifyAccountsOverviewAfterLogin1() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Verify Account Overview page
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(),
				"Accounts Overview page is not displayed after login");
	}

	@Test
	public void verifyUserLogoutFunctionality() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Logout
		LoginPage Logout = accountOverviewPage.logout();

		// Verify Login page is displayed
		Assert.assertTrue(driver.findElement(By.name("username")).isDisplayed(),
				"Login page is not displayed after logout");
	}

	@Test
	public void verifyTotalBalanceDisplay() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Verify Account Overview page
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");

		// Find Total row
		String totalBalance = driver.findElement(By.xpath("//td[contains(text(),'Total')]/following-sibling::td"))
				.getText();

		System.out.println("Total Balance: " + totalBalance);

		// Verify total balance is displayed
		Assert.assertFalse(totalBalance.trim().isEmpty(), "Total Balance is not displayed");

		// Verify currency format
		Assert.assertTrue(totalBalance.contains("$"), "Total Balance is not displayed in currency format");
	}

	@Test
	public void verifyAccountOverviewPageAccess() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Verify Account Overview page
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Account Overview page cannot be accessed");
	}

	@Test
	public void verifyAllExistingAccountsAreListed() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get all account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		// Verify accounts are available
		Assert.assertNotNull(accountIds, "Account ID list is null");

		Assert.assertFalse(accountIds.isEmpty(), "No accounts are displayed");

		// Verify every account ID
		for (String accountId : accountIds) {

			Assert.assertFalse(accountId.trim().isEmpty(), "Account ID is blank");

			System.out.println("Account ID displayed: " + accountId);
		}
	}

	@Test
	public void verifyNavigationFromAccountNumberToAccountDetails() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get all account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		// Verify account exists
		Assert.assertFalse(accountIds.isEmpty(), "No account is available");

		// Get first account
		String accountId = accountIds.get(0);

		System.out.println("Clicking Account Number: " + accountId);

		// Click account number
		driver.findElement(By.xpath("//table[@id='accountTable']//a[text()='" + accountId + "']")).click();

		// Verify Account Activity page
		Assert.assertTrue(driver.getCurrentUrl().contains("activity"),
				"Account Activity / Details page is not displayed");

	}

	@Test
	public void verifyBalanceAndAvailableAmountForAccount() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get all account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		// Verify account is available
		Assert.assertFalse(accountIds.isEmpty(), "No account is displayed");

		// Get first account ID
		String accountId = accountIds.get(0);

		System.out.println("Account ID: " + accountId);

		// Get Balance using existing POM method
		double balance = accountOverviewPage.getBalanceForAccount(accountId);

		System.out.println("Balance: $" + balance);

		// Get Available Amount from Account Overview table
		WebElement availableAmountCell = driver.findElement(By.xpath("//a[text()='" + accountId + "']/../../td[3]"));

		String availableAmountText = availableAmountCell.getText();

		System.out.println("Available Amount: " + availableAmountText);

		// Verify Balance
		Assert.assertTrue(balance >= 0, "Balance should not be negative");

		// Verify Available Amount is displayed
		Assert.assertFalse(availableAmountText.trim().isEmpty(),
				"Available Amount is not displayed for account: " + accountId);

		// Verify Available Amount contains currency symbol
		Assert.assertTrue(availableAmountText.contains("$"), "Available Amount is not displayed in currency format");
	}

	@Test
	public void verifyAccountBalanceDisplay() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get all account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		Assert.assertFalse(accountIds.isEmpty(), "No accounts are displayed");

		// Get first account
		String accountId = accountIds.get(0);

		System.out.println("Account ID: " + accountId);

		// Get balance
		double balance = accountOverviewPage.getBalanceForAccount(accountId);

		System.out.println("Account Balance: $" + balance);

		// Verify balance
		Assert.assertTrue(balance >= 0, "Account balance is invalid for account: " + accountId);
	}

	@Test
	public void verifyNavigationToAccountDetailsPage() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		Assert.assertFalse(accountIds.isEmpty(), "No accounts are available");

		// Get first account ID
		String accountId = accountIds.get(0);

		System.out.println("Clicking Account Number: " + accountId);

		// Click account number
		driver.findElement(By.xpath("//table[@id='accountTable']//a[text()='" + accountId + "']")).click();

		// Verify Account Details / Activity page
		Assert.assertTrue(driver.getCurrentUrl().contains("activity"), "Account Details page is not displayed");
	}

	@Test
	public void verifyTransactionDetailsFromAccountActivity() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		Assert.assertFalse(accountIds.isEmpty(), "No accounts are available");

		// Get first account
		String accountId = accountIds.get(0);

		System.out.println("Opening Account Activity for: " + accountId);

		// Navigate to Account Activity
		driver.findElement(By.xpath("//table[@id='accountTable']//a[text()='" + accountId + "']")).click();

		// Verify transaction table
		WebElement transactionTable = driver.findElement(By.id("transactionTable"));

		Assert.assertTrue(transactionTable.isDisplayed(), "Transaction details are not displayed");

		System.out.println("Transaction details are displayed successfully");
	}

	@Test
	public void verifyTotalBalanceOfAllAccounts() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Get all account IDs
		List<String> accountIds = accountOverviewPage.getAllAccountIds();

		Assert.assertFalse(accountIds.isEmpty(), "No accounts are displayed");

		// Calculate total balance
		double calculatedTotal = 0.0;

		for (String accountId : accountIds) {

			double balance = accountOverviewPage.getBalanceForAccount(accountId);

			System.out.println("Account: " + accountId + " Balance: $" + balance);

			calculatedTotal += balance;
		}

		System.out.println("Calculated Total Balance: $" + calculatedTotal);

		// Get Total Balance displayed on page
		String totalBalanceText = driver.findElement(By.xpath("//td[contains(text(),'Total')]/following-sibling::td"))
				.getText();

		double displayedTotal = Double.parseDouble(totalBalanceText.replace("$", "").replace(",", "").trim());

		System.out.println("Displayed Total Balance: $" + displayedTotal);

		// Compare calculated and displayed total
		Assert.assertEquals(displayedTotal, calculatedTotal, 0.01,
				"Total balance does not match sum of all account balances");
	}

	@Test
	public void verifyLoginWithValidCredentials() {

		// Login
		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login("Dhanashri", "gaikwad@29");

		// Verify successful login
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(),
				"Login failed or Account Overview page is not displayed");
	}

	@Test
	public void verifyAccountBalanceAndTotalBalance() {

		// Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		// Verify Account Overview page
		Assert.assertTrue(accountOverviewPage.isAccountOverviewPageDisplayed(),
				"Account Overview page is not displayed");

		// Verify accounts are displayed
		Assert.assertTrue(accountOverviewPage.getAccountCount() > 0, "No accounts are displayed");

		// Get Total Balance
		String totalBalance = accountOverviewPage.getTotalBalance();

		// Verify Total Balance is displayed
		Assert.assertNotNull(totalBalance, "Total Balance is not displayed");

		Assert.assertFalse(totalBalance.trim().isEmpty(), "Total Balance is empty");

		System.out.println("Total Balance: " + totalBalance);
	}

	@Test
	public void verifyTransferFundsUpdatesAccountBalance() {

		// Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		// Get source account and its initial balance
		String fromAccount = accountOverviewPage.getFirstAccountNumber();
		double initialBalance = accountOverviewPage.getAccountBalance(fromAccount);

		// Navigate to Transfer Funds
		TransferFundsPage transferFundsPage = accountOverviewPage.goToTransferFunds();

		// Transfer amount
		double transferAmount = 100.00;

		transferFundsPage.selectFromAccount(fromAccount);
		transferFundsPage.selectToAccount(fromAccount); // replace with another account
		transferFundsPage.enterAmount(String.valueOf(transferAmount));
		transferFundsPage.clickTransfer();

		// Navigate back to Account Overview
		accountOverviewPage = transferFundsPage.clickAccountOverview();

		// Get updated balance
		double updatedBalance = accountOverviewPage.getAccountBalance(fromAccount);

		// Verify balance
		Assert.assertEquals(updatedBalance, initialBalance - transferAmount,
				"Account balance was not updated correctly after transfer");
	}

	@Test
	public void verifyAccountNavigationFromOverviewToActivity() {

		// Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		// Verify Account Overview page
		Assert.assertTrue(accountOverviewPage.isAccountOverviewPageDisplayed(),
				"Account Overview page is not displayed");

		// Get Account Number
		String accountNumber = accountOverviewPage.getFirstAccountNumber();

		Assert.assertNotNull(accountNumber, "Account number is not displayed");

		// Click Account Number
		accountOverviewPage.clickAccount(accountNumber);

		// Verify Account Activity page
		Assert.assertTrue(accountOverviewPage.isAccountActivityPageDisplayed(),
				"Account Activity page is not displayed");
	}

	@Test
	public void verifyTransferFundsBetweenAccounts() {

		// Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		String fromAccount = accountOverviewPage.getFirstAccountNumber();

		String toAccount = accountOverviewPage.getSecondAccountNumber();

		double fromInitialBalance = accountOverviewPage.getAccountBalance(fromAccount);

		double toInitialBalance = accountOverviewPage.getAccountBalance(toAccount);

		// Transfer Funds
		TransferFundsPage transferFundsPage = accountOverviewPage.goToTransferFunds();

		double amount = 100.00;

		transferFundsPage.selectFromAccount(fromAccount);
		transferFundsPage.selectToAccount(toAccount);
		transferFundsPage.enterAmount(String.valueOf(amount));
		transferFundsPage.clickTransfer();

		// Return to Account Overview
		accountOverviewPage = transferFundsPage.clickAccountOverview();

		// Get updated balances
		double fromUpdatedBalance = accountOverviewPage.getAccountBalance(fromAccount);

		double toUpdatedBalance = accountOverviewPage.getAccountBalance(toAccount);

		// Verify source account
		Assert.assertEquals(fromUpdatedBalance, fromInitialBalance - amount, "Source account balance is incorrect");

		// Verify destination account
		Assert.assertEquals(toUpdatedBalance, toInitialBalance + amount, "Destination account balance is incorrect");
	}

	@Test
	public void verifyAccountTransactionDetails() {

		// Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		// Get first account number
		String accountId = accountOverviewPage.getFirstAccountNumber();

		// Click account number
		accountOverviewPage.clickAccount(accountId);

		// Verify Account Activity page
		Assert.assertTrue(driver.getTitle().contains("ParaBank"), "Account Activity page is not displayed");
	}

	@Test
	public void verifyAccountActivityNavigationFromAccountOverview() {

		// Step 1: Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Step 2: Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		Assert.assertTrue(accountOverviewPage.isAccountOverviewPageDisplayed(),
				"Account Overview page is not displayed");

		// Step 3: Get first account number
		String accountId = accountOverviewPage.getFirstAccountNumber();

		// Step 4: Click account number
		accountOverviewPage.clickAccount(accountId);

		// Step 5: Verify Account Activity page
		Assert.assertTrue(accountOverviewPage.isAccountActivityPageDisplayed(),
				"Account Activity page is not displayed");
	}

	@Test
	public void verifyNavigationFromAccountOverviewToTransferFunds() {

		// Login
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login("Dhanashri", "gaikwad@29");

		// Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		// Navigate to Transfer Funds
		TransferFundsPage transferFundsPage = accountOverviewPage.goToTransferFunds();

		// Verify Transfer Funds page
		Assert.assertTrue(transferFundsPage.isTransferSuccessful(), "Transfer Funds page is not displayed");
	}

	@Test
	public void verifyEndToEndFlowFromLoginToLogout() {

		// Step 1: Login
		LoginPage loginPage = new LoginPage(driver);

		loginPage.login("Dhanashri", "gaikwad@29");

		// Step 2: Account Overview
		AccountOverviewPage accountOverviewPage = new AccountOverviewPage(driver);

		Assert.assertTrue(accountOverviewPage.isAccountOverviewPageDisplayed(),
				"Account Overview page is not displayed after login");

		// Step 3: Verify accounts are displayed
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts table is not displayed");

		// Step 4: Get first account number
		String accountId = accountOverviewPage.getFirstAccountNumber();

		Assert.assertFalse(accountId.isEmpty(), "Account number is not displayed");

		System.out.println("Selected Account ID: " + accountId);

		// Step 5: Navigate to Account Activity
		accountOverviewPage.clickAccount(accountId);

		// Step 6: Verify Account Activity page
		Assert.assertTrue(accountOverviewPage.isAccountActivityPageDisplayed(),
				"Account Activity page is not displayed");

		// Step 7: Verify Transaction table
		Assert.assertTrue(accountOverviewPage.isTransactionTableDisplayed(), "Transaction table is not displayed");

		// Step 8: Verify transaction exists
		Assert.assertTrue(accountOverviewPage.isTransactionDisplayed(),
				"No transaction is displayed for the selected account");

		// Step 9: Print transaction details
		System.out.println("Transactions: " + accountOverviewPage.getTransactionDetails());

		// Step 10: Logout
		LoginPage loginPageAfterLogout = accountOverviewPage.logout();

		// Step 11: Verify Login page
		Assert.assertTrue(loginPageAfterLogout.isLoginPageDisplayed(), "Login page is not displayed after logout");
	}
}