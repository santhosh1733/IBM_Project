package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import com.parabank.utils.ConfigReader;

import org.testng.Assert;
import org.testng.annotations.Test;


public class LoginTest extends BaseTest {

	private static final String VALID_USERNAME = ConfigReader.get("username");
    private static final String VALID_PASSWORD = ConfigReader.get("password");

    @Test(description = "Verify successful login with valid credentials")
    public void verifySuccessfulLogin() {
        AccountOverviewPage overview = new LoginPage(driver).login(VALID_USERNAME, VALID_PASSWORD);
        Assert.assertTrue(overview.isAccountsTableDisplayed(),
                "Expected Accounts Overview table to be displayed after login");
}
