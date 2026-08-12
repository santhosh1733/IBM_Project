package com.parabank.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.FindTransactionsPage;
import com.parabank.pages.LoginPage;

public class FindTransactionsTest extends BaseTest {

	// Test Data
	private final String USERNAME = "AichhikaTest12";
	private final String PASSWORD = "Aichhika@12";

	private final String TRANSACTION_ID = "16474";
	private final String INVALID_TRANSACTION_ID = "999999999";

	private final String AMOUNT = "100.00";
	private final String INVALID_AMOUNT = "-100";

	private final String DATE = "08-12-2026";
	private static final String FROM_DATE = "08-01-2026";
	private static final String TO_DATE = "08-12-2026";

	private static final String NO_TRANSACTION_DATE = "01-01-2020";

	// TC001 - Verify Find Transactions page opens successfully
	@Test
	public void verifyFindTransactionsPageOpensSuccessfully() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		Assert.assertNotNull(findTransactionsPage, "Find Transactions page was not opened");
	}

	// TC002 - Verify search by Transaction ID
	@Test
	public void verifySearchByTransactionId() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		findTransactionsPage.searchByTransactionId(TRANSACTION_ID);
		// Temporary debugging
		System.out.println("========== TC002 COMPLETED ==========");
	}

	// TC003 - Verify search by Amount
	@Test
	public void verifySearchByAmount() throws InterruptedException {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		findTransactionsPage.searchByAmount(AMOUNT);
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found for amount: " + AMOUNT);
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Amount search returned no results");
	}

	// TC004 - Verify invalid Transaction ID returns no results
	@Test
	public void verifyInvalidTransactionIdSearch() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		findTransactionsPage.searchByTransactionId(INVALID_TRANSACTION_ID);
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0, "Invalid Transaction ID returned unexpected results");
	}

	// TC005 - Verify invalid Amount returns no results
	@Test
	public void verifyInvalidAmountSearch() throws InterruptedException {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		findTransactionsPage.searchByAmount(INVALID_AMOUNT);
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0, "Invalid amount returned unexpected results");
	}

	// TC006 - E2E: Verify transaction details using Transaction ID
	@Test
	public void verifyTransactionDetailsUsingTransactionId() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search transaction using Transaction ID
		findTransactionsPage.searchByTransactionId(TRANSACTION_ID);
		// Verify transaction result is displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found for Transaction ID: " + TRANSACTION_ID);
		// Verify at least one result is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Transaction search returned no results");
		System.out.println("TC006 PASSED - Transaction found for ID: " + TRANSACTION_ID);
	}

	// TC007 - Verify search by Date
	@Test
	public void verifySearchByDate() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search transaction by date
		findTransactionsPage.searchByDate(DATE);
		// Verify results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found for date: " + DATE);
		// Verify at least one result is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Date search returned no results");
		System.out.println("TC007 PASSED - Transaction found for date: " + DATE);
	}

	// TC008 - Verify search by Date Range

	@Test
	public void verifySearchByDateRange() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search transaction by date range
		findTransactionsPage.searchByDateRange(FROM_DATE, TO_DATE);
		// Verify results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found between " + FROM_DATE + " and " + TO_DATE);
		// Verify at least one result is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Date range search returned no results");
		System.out.println("TC008 PASSED - Transaction found between " + FROM_DATE + " and " + TO_DATE);
	}

	// TC009 - Verify no results are displayed for a date with no transactions

	@Test
	public void verifyNoResultsForDate() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search using a date with no transactions
		findTransactionsPage.searchByDate(NO_TRANSACTION_DATE);
		// Verify no results are returned
		Assert.assertFalse(findTransactionsPage.hasResults(), "Transaction results were found for date: " + NO_TRANSACTION_DATE);
		// Verify result count is zero
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0, "Expected zero transactions for date: " + NO_TRANSACTION_DATE);
		System.out.println("TC009 PASSED - No transactions found for date: " + NO_TRANSACTION_DATE);
	}
}