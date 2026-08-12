package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Owner: M1
 * TODO: implement tests for TC covering Registration + Login/Auth module
 * (see Jira Story 1). Follow the pattern in RequestLoanTest.java.
 */
public class LoginTest extends BaseTest {

    @Test(description = "Verify successful login with valid credentials")
    public void verifySuccessfulLogin() {
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        Assert.assertTrue(overview.isAccountsTableDisplayed(),
                "Expected Accounts Overview table to be displayed after login");
    }

    @Test(description = "Verify failed login with invalid credentials shows error")
    public void verifyFailedLoginShowsError() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("invalid_user", "wrong_password");
        Assert.assertTrue(loginPage.isErrorDisplayed(), "Expected an error message on invalid login");
    }

    @Test(description = "Verify logout terminates session and returns to login page")
    public void verifyLogout() {
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        LoginPage loginPage = overview.logout();
        Assert.assertTrue(loginPage.isErrorDisplayed() == false,
                "Expected to land back on a clean login page after logout");
    }
}