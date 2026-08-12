package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import com.parabank.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Login module tests.
 *
 * Credentials are read from the same configuration keys used by the other
 * modules (including BillPayTest). They can also be overridden at runtime:
 *
 * mvn test -Dtest=LoginTest -Dparabank.username=<user> -Dparabank.password=<pass>
 */
public class LoginTest extends BaseTest {

    @Test(description = "Verify successful login with valid credentials")
    public void verifySuccessfulLogin() {
        String username = System.getProperty(
                "parabank.username", ConfigReader.get("parabank.username"));
        String password = System.getProperty(
                "parabank.password", ConfigReader.get("parabank.password"));

        AccountOverviewPage overview = new LoginPage(driver).login(username, password);

        Assert.assertTrue(overview.isAccountsTableDisplayed(),
                "Expected Accounts Overview table to be displayed after login. "
                        + "Check parabank.username/parabank.password in config.properties.");
    }
}
