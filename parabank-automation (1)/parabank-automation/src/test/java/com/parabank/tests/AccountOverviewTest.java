package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class AccountOverviewTest extends BaseTest {

	
	@Test
	public void verifyIntegrationBetweenLoginAndAccountDetailsModule() {

	    // Login to the application
	    LoginPage loginPage = new LoginPage(driver);
	    loginPage.login("Dhanashri", "dhanno@1929");

	    // Verify Account Overview page is displayed
	    AccountOverviewPage accountOverviewPage =
	            new AccountOverviewPage(driver);

	    Assert.assertTrue(
	            accountOverviewPage.isAccountsTableDisplayed(),
	            "Account Overview page is not displayed after login"
	    );

	    // Get the first available account number
	    String accountId =
	            accountOverviewPage.getAllAccountIds().get(0);

	    // Click on the account number
//	    accountOverviewPage.clickAccount(accountId);

	    // Verify navigation to Account Details/Activity page
	    Assert.assertTrue(
	            driver.getCurrentUrl().contains("activity"),
	            "Account Details/Activity page is not displayed"
	    );
	}

}
