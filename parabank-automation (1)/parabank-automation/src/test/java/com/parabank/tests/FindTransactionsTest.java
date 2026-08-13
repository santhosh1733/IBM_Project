package com.parabank.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.FindTransactionsPage;
import com.parabank.pages.LoginPage;

public class FindTransactionsTest extends BaseTest {

	// Test Data
	private final String USERNAME = "Test123";
	private final String PASSWORD = "Test123";

	private final String TRANSACTION_ID = "14587";
	private final String INVALID_TRANSACTION_ID = "999999999";

	private final String AMOUNT = "100.00";
	private final String INVALID_AMOUNT = "-100";

	private final String DATE = "08-12-2026";
	private static final String FROM_DATE = "08-01-2026";
	private static final String TO_DATE = "08-12-2026";

	private static final String SINGLE_DATE = "08-12-2026";

	private static final String NO_TRANSACTION_DATE = "01-01-2020";

	private final String COMMON_AMOUNT = "100.00";

	private final String SPECIAL_CHAR_TRANSACTION_ID = "@#$%";

	private final String FUTURE_DATE = "12-31-2099";

	private final String INVALID_DATE_FORMAT = "2026/08/12";

	private final String TRANSFER_TRANSACTION_ID = "14476";

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
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0,
				"Invalid Transaction ID returned unexpected results");
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
		Assert.assertTrue(findTransactionsPage.hasResults(),
				"No transaction found for Transaction ID: " + TRANSACTION_ID);
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
		Assert.assertTrue(findTransactionsPage.hasResults(),
				"No transaction found between " + FROM_DATE + " and " + TO_DATE);
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
		Assert.assertFalse(findTransactionsPage.hasResults(),
				"Transaction results were found for date: " + NO_TRANSACTION_DATE);
		// Verify result count is zero
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0,
				"Expected zero transactions for date: " + NO_TRANSACTION_DATE);
		System.out.println("TC009 PASSED - No transactions found for date: " + NO_TRANSACTION_DATE);
	}

	// Functional:10- Verify search with same From Date and To Date
	@Test
	public void verifySearchWithSameFromAndToDate() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Enter the same date in From Date and To Date
		findTransactionsPage.searchByDateRange(SINGLE_DATE, SINGLE_DATE);
		// Verify results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found for date: " + SINGLE_DATE);
		// Verify at least one transaction is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Same From Date and To Date returned no results");
		System.out.println("FUNCTIONAL PASSED - Transactions found for single date: " + SINGLE_DATE);
	}

	// Functional: 11 - Verify all returned transactions match searched amount
	@Test
	public void verifyAllTransactionsMatchSearchedAmount() throws InterruptedException {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search using amount
		findTransactionsPage.searchByAmount(AMOUNT);
		// Verify results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transactions found for amount: " + AMOUNT);
		// Verify every returned transaction matches the searched amount
		Assert.assertTrue(findTransactionsPage.areAllResultsForAmountDisplayed("$" + AMOUNT),
				"One or more returned transactions do not match amount: " + AMOUNT);
		System.out.println("FUNCTIONAL PASSED - All returned transactions match amount: " + AMOUNT);
	}

	// Adhoc:12 - Verify search using special characters in Transaction ID
	@Test
	public void verifySearchUsingSpecialCharactersInTransactionId() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search using special characters
		findTransactionsPage.searchByTransactionId(SPECIAL_CHAR_TRANSACTION_ID);
		// Verify no matching transaction is returned
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0,
				"Special characters returned unexpected transaction results");
		System.out.println("ADHOC PASSED - Special character input handled correctly");
	}

	// Adhoc:13 - Verify search using a future date
	@Test
	public void verifySearchUsingFutureDate() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search using a future date
		findTransactionsPage.searchByDate(FUTURE_DATE);
		// Verify no transactions are returned
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0,
				"Transactions were unexpectedly found for future date: " + FUTURE_DATE);
		System.out.println("ADHOC PASSED - No transactions found for future date: " + FUTURE_DATE);
	}

	// Adhoc:14 - Verify new search updates previous search results
	@Test
	public void verifyNewSearchUpdatesPreviousResults() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// First search
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		findTransactionsPage.searchByDate(DATE);
		Assert.assertTrue(findTransactionsPage.hasResults(), "First search returned no results for date: " + DATE);
		int firstResultCount = findTransactionsPage.getResultCount();
		System.out.println("First search result count: " + firstResultCount);
		// Navigate to Find Transactions again
		FindTransactionsPage secondFindTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Second search
		secondFindTransactionsPage.searchByDate(NO_TRANSACTION_DATE);
		// Verify second search has no results
		Assert.assertFalse(secondFindTransactionsPage.hasResults(), "Previous transaction results were retained");
		Assert.assertEquals(secondFindTransactionsPage.getResultCount(), 0, "Expected zero results for second search");
		System.out.println("ADHOC PASSED - New search returned updated results");
	}

	// Adhoc:15 - Verify search using invalid date format
	@Test
	public void verifySearchUsingInvalidDateFormat() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search using invalid date format
		findTransactionsPage.searchByDate(INVALID_DATE_FORMAT);
		// Verify no valid transaction result is returned
		Assert.assertEquals(findTransactionsPage.getResultCount(), 0,
				"Invalid date format returned unexpected transaction results");
		System.out.println("ADHOC PASSED - Invalid date format handled correctly: " + INVALID_DATE_FORMAT);
	}

	// Integration:16 - Verify transferred transaction appears in Find Transactions
	@Test
	public void verifyTransferredTransactionInFindTransactions() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search for the transaction created by Transfer Funds
		findTransactionsPage.searchByTransactionId(TRANSFER_TRANSACTION_ID);
		// Verify transaction result is displayed
		Assert.assertTrue(findTransactionsPage.hasResults(),
				"Transferred transaction was not found in Find Transactions");
		// Verify transaction description
		Assert.assertTrue(findTransactionsPage.isTransactionDescriptionDisplayed("Funds Transfer Sent"),
				"Funds Transfer Sent transaction was not displayed");
		// Verify transferred amount
		Assert.assertTrue(findTransactionsPage.isTransactionAmountDisplayed("$100.00"),
				"Transferred amount $100.00 was not displayed");
		System.out.println("INTEGRATION PASSED - Transferred transaction found in " + "Find Transactions");
	}

	// Integration:17 - Verify transferred transaction can be searched by amount
	@Test
	public void verifyTransferredTransactionByAmount() throws InterruptedException {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search transferred transaction by amount
		findTransactionsPage.searchByAmount(COMMON_AMOUNT);
		// Verify transaction results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(),
				"No transaction found for transferred amount: " + COMMON_AMOUNT);
		// Verify at least one transaction is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Transferred amount search returned no results");
		// Verify Funds Transfer Sent is present
		Assert.assertTrue(findTransactionsPage.isTransactionDescriptionDisplayed("Funds Transfer Sent"),
				"Funds Transfer Sent transaction was not found for amount: " + COMMON_AMOUNT);
		// Verify transferred amount is displayed
		Assert.assertTrue(findTransactionsPage.isTransactionAmountDisplayed("$100.00"),
				"Transferred amount $100.00 was not displayed");
		System.out.println("INTEGRATION PASSED - Transferred transaction found by amount: " + COMMON_AMOUNT);
	}

	// Integration:18 - Verify transferred transaction appears in date range search
	@Test
	public void verifyTransferredTransactionByDateRange() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search transferred transaction using date range
		findTransactionsPage.searchByDateRange(FROM_DATE, TO_DATE);
		// Verify results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(),
				"No transactions found between " + FROM_DATE + " and " + TO_DATE);
		// Verify at least one transaction is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Date range search returned no transactions");
		// Verify transferred transaction is present
		Assert.assertTrue(findTransactionsPage.isTransactionDescriptionDisplayed("Funds Transfer Sent"),
				"Funds Transfer Sent transaction was not found in the date range");
		// Verify transferred amount
		Assert.assertTrue(findTransactionsPage.isTransactionAmountDisplayed("$100.00"),
				"Transferred amount $100.00 was not found in the date range");
		System.out.println("INTEGRATION PASSED - Transferred transaction found " + "within date range: " + FROM_DATE
				+ " to " + TO_DATE);
	}

	// E2E:19 - Verify transaction details from Find Transactions result
	@Test
	public void verifyTransactionDetailsFromFindTransactions() {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search for a valid transaction
		findTransactionsPage.searchByTransactionId(TRANSACTION_ID);
		// Verify search result is displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found for ID: " + TRANSACTION_ID);
		// Click the transaction from search results
		findTransactionsPage.clickFirstTransaction();
		// Verify transaction details page is displayed
		Assert.assertTrue(findTransactionsPage.isTransactionDetailsPageDisplayed(),
				"Transaction details page was not displayed");
		System.out.println(
				"E2E PASSED - Transaction details opened successfully " + "for transaction: " + TRANSACTION_ID);
	}

	// E2E:20 - Verify transaction details by searching with Amount
	@Test
	public void verifyTransactionDetailsByAmount() throws InterruptedException {

		LoginPage loginPage = new LoginPage(driver);
		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);
		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");
		// Navigate to Find Transactions
		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();
		// Search transaction by amount
		findTransactionsPage.searchByAmount(AMOUNT);
		// Verify search results are displayed
		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction found for amount: " + AMOUNT);
		// Verify at least one result is returned
		Assert.assertTrue(findTransactionsPage.getResultCount() > 0, "Amount search returned no results");
		// Click the first transaction from the search results
		findTransactionsPage.clickFirstTransaction();
		// Verify transaction details page is displayed
		Assert.assertTrue(findTransactionsPage.isTransactionDetailsPageDisplayed(),
				"Transaction details page was not displayed");
		System.out.println("E2E PASSED - Transaction details opened successfully " + "for amount: " + AMOUNT);
	}
}