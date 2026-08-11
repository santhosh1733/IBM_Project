package com.parabank.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.FindTransactionsPage;
import com.parabank.pages.LoginPage;

public class FindTransactionsTest extends BaseTest {

	// Test Data
	private final String USERNAME = "Aichhika12";
	private final String PASSWORD = "Mumdad@01";

	private final String TRANSACTION_ID = "19471";
	private final String AMOUNT = "100";

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

		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();

		findTransactionsPage.searchByTransactionId(TRANSACTION_ID);

		Assert.assertTrue(findTransactionsPage.hasResults(),
				"No transaction results found for Transaction ID: " + TRANSACTION_ID);
	}

	// TC003 - Verify search by Amount
	@Test
	public void verifySearchByAmount() {

		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);

		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();

		findTransactionsPage.searchByAmount(AMOUNT);

		Assert.assertTrue(findTransactionsPage.hasResults(), "No transaction results found for amount: " + AMOUNT);
	}

	// TC004 - Verify Transaction ID search returns results
	@Test
	public void verifyTransactionIdSearchReturnsResults() {

		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);

		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();

		findTransactionsPage.searchByTransactionId(TRANSACTION_ID);

		int resultCount = findTransactionsPage.getResultCount();

		System.out.println("Transaction ID Search Result Count: " + resultCount);

		Assert.assertTrue(resultCount > 0, "Transaction ID search returned no results");
	}

	// TC005 - Verify Amount search returns results
	@Test
	public void verifyAmountSearchReturnsResults() {

		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);

		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();

		findTransactionsPage.searchByAmount(AMOUNT);

		int resultCount = findTransactionsPage.getResultCount();

		System.out.println("Amount Search Result Count: " + resultCount);

		Assert.assertTrue(resultCount > 0, "Amount search returned no results");
	}

	// TC006 - Verify invalid Transaction ID returns no results
	@Test
	public void verifyInvalidTransactionIdSearch() {

		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);

		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();

		String invalidTransactionId = "999999999";

		findTransactionsPage.searchByTransactionId(invalidTransactionId);

		int resultCount = findTransactionsPage.getResultCount();

		System.out.println("Invalid Transaction ID Result Count: " + resultCount);

		Assert.assertEquals(resultCount, 0, "Invalid Transaction ID returned unexpected results");
	}

	// TC007 - Verify Find Transactions page can search by ID and Amount
	@Test
	public void verifyFindTransactionSearchFlow() {

		LoginPage loginPage = new LoginPage(driver);

		AccountOverviewPage accountOverviewPage = loginPage.login(USERNAME, PASSWORD);

		Assert.assertTrue(accountOverviewPage.isAccountsTableDisplayed(), "Accounts Overview page is not displayed");

		FindTransactionsPage findTransactionsPage = accountOverviewPage.goToFindTransactions();

		Assert.assertNotNull(findTransactionsPage, "Find Transactions page was not opened");

		// Search by Transaction ID
		findTransactionsPage.searchByTransactionId(TRANSACTION_ID);

		Assert.assertTrue(findTransactionsPage.hasResults(), "Transaction ID search did not return results");

		System.out.println("Transaction ID search completed successfully.");
	}
}