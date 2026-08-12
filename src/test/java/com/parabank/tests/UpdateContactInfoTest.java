package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import com.parabank.pages.RequestLoanPage;
import com.parabank.pages.TransferFundsPage;
import com.parabank.pages.UpdateContactInfoPage;
import com.parabank.utils.ConfigReader;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Owner: M5
 * Full Update Contact Info test suite -- 20 cases across 4 TestNG groups:
 *   smoke        (5)  - fast critical-path checks
 *   functional   (7)  - module-level business rules, in isolation
 *   integration  (5)  - Update Contact Info's persistence across other modules
 *   e2e          (3)  - full multi-module user journeys
 *
 * All test data (addresses, phone numbers, transfer amounts) comes from the
 * "UpdateContactInfoSuite" sheet in TestData.xlsx, fetched by TestCaseId via
 * testData("UpdateContactInfoSuite", "<ID>") -- see BaseTest. No business
 * values are hardcoded in this file; only the TestCaseId strings that map a
 * test method to its Excel row.
 *
 * Run a single group from the command line:
 *   mvn test -Dgroups=smoke
 *   mvn test -Dgroups=functional
 *   mvn test -Dgroups=integration
 *   mvn test -Dgroups=e2e
 *
 * UpdateContactInfoPage's locators are still a starter scaffold -- verify
 * them against the live DOM before relying on these tests. Same for
 * TransferFundsPage / RequestLoanPage where used in the integration/e2e
 * sections below.
 */
public class UpdateContactInfoTest extends BaseTest {
	//private static final String USERNAME = ConfigReader.get("username");
    //private static final String PASSWORD = ConfigReader.get("password");

    // =========================================================================
    // SMOKE (5) -- fast, critical-path only.
    // =========================================================================

    @Test(groups = "smoke", description = "SMOKE_01: Verify Update Contact Info menu is accessible after login")
    public void smoke01_updateContactInfoMenuAccessible() {
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        Assert.assertTrue(overview.isAccountsTableDisplayed(), "Expected Accounts Overview to load after login");
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();
        Assert.assertNotNull(contactPage, "Update Contact Info page should be reachable from the main menu");
    }

    @Test(groups = "smoke", description = "SMOKE_02: Verify Update Contact Info page renders with existing data pre-filled")
    public void smoke02_pageLoadsWithPrefilledData() {
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        Assert.assertFalse(contactPage.getFirstNameValue().isEmpty(), "Expected first name field to be pre-filled");
    }

    @Test(groups = "smoke", description = "SMOKE_03: Verify a basic contact update submits without error")
    public void smoke03_basicUpdateSubmitsCleanly() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "SMOKE_03");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();

        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected update confirmation to display");
    }

    @Test(groups = "smoke", description = "SMOKE_04: Verify re-submitting unchanged data still succeeds")
    public void smoke04_noOpUpdateSucceeds() {
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        // Re-submits whatever is currently pre-filled -- no Excel data needed,
        // this test is deliberately about NOT changing the values.
        Map<String, String> current = contactPage.getAllFieldValues();
        contactPage.updateAddress(current.get("street"), current.get("city"), current.get("state"),
                        current.get("zip"), current.get("phone"))
                .submitUpdate();

        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected update to succeed even with unchanged values");
    }

    @Test(groups = "smoke", description = "SMOKE_05: Verify the app remains stable and navigable after an update")
    public void smoke05_appStableAfterUpdate() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "SMOKE_05");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Precondition: update should succeed");

        driver.navigate().to(driver.getCurrentUrl().replace("updateprofile.htm", "overview.htm"));
        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);
        Assert.assertTrue(refreshedOverview.isAccountsTableDisplayed(),
                "Expected to navigate back to Account Overview cleanly after an update");
    }

    // =========================================================================
    // FUNCTIONAL (7) -- module-level business rules, in isolation.
    // =========================================================================

    @Test(groups = "functional", description = "FUNC_01: Verify page pre-fills existing customer details correctly")
    public void func01_prefillMatchesExistingData() {
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        Map<String, String> values = contactPage.getAllFieldValues();
        Assert.assertFalse(values.get("firstName").isEmpty(), "First name should be pre-filled");
        Assert.assertFalse(values.get("lastName").isEmpty(), "Last name should be pre-filled");
        Assert.assertFalse(values.get("street").isEmpty(), "Street should be pre-filled");
    }

    @Test(groups = "functional", description = "FUNC_02: Verify successful update of all editable fields")
    public void func02_allFieldsUpdateSuccessfully() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_02");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected all-field update to succeed");

        driver.navigate().to(driver.getCurrentUrl());
        UpdateContactInfoPage refreshedPage = new UpdateContactInfoPage(driver);
        Assert.assertEquals(refreshedPage.getStreetValue(), data.get("Street"), "Street should reflect the update");
        Assert.assertEquals(refreshedPage.getCityValue(), data.get("City"), "City should reflect the update");
    }

    @Test(groups = "functional", description = "FUNC_03: Verify validation error when Street is left empty")
    public void func03_emptyStreetValidation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_03");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .clearStreet()
                .submitUpdate();

        Assert.assertFalse(contactPage.isUpdateSuccessful(), "Update should not succeed with an empty street field");
    }

    @Test(groups = "functional", description = "FUNC_04: Verify validation error when City is left empty")
    public void func04_emptyCityValidation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_04");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .clearCity()
                .submitUpdate();

        Assert.assertFalse(contactPage.isUpdateSuccessful(), "Update should not succeed with an empty city field");
    }

    @Test(groups = "functional", description = "FUNC_05: Verify validation error when State is left empty")
    public void func05_emptyStateValidation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_05");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .clearState()
                .submitUpdate();

        Assert.assertFalse(contactPage.isUpdateSuccessful(), "Update should not succeed with an empty state field");
    }

    @Test(groups = "functional", description = "FUNC_06: Verify validation error when Zip Code is left empty")
    public void func06_emptyZipValidation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_06");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .clearZip()
                .submitUpdate();

        Assert.assertFalse(contactPage.isUpdateSuccessful(), "Update should not succeed with an empty zip field");
    }

    @Test(groups = "functional", description = "FUNC_07: Verify validation error when Phone Number is left empty")
    public void func07_emptyPhoneValidation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_07");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .clearPhone()
                .submitUpdate();

        Assert.assertFalse(contactPage.isUpdateSuccessful(), "Update should not succeed with an empty phone field");
    }

    // =========================================================================
    // INTEGRATION (5) -- Update Contact Info's persistence across session and
    // other module actions.
    // =========================================================================

    @Test(groups = "integration", description = "INT_01: Verify updated details persist after logout/re-login")
    public void int01_persistsAcrossLogoutLogin() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "INT_01");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Precondition: update should succeed");

        LoginPage loginPage = new AccountOverviewPage(driver).logout();
        AccountOverviewPage reloginOverview = loginPage.login(USERNAME, PASSWORD);
        UpdateContactInfoPage reloadedContactPage = reloginOverview.goToUpdateContactInfo();

        Assert.assertEquals(reloadedContactPage.getStreetValue(), data.get("Street"),
                "Updated street should persist after logout/login");
        Assert.assertEquals(reloadedContactPage.getPhoneValue(), data.get("Phone"),
                "Updated phone should persist after logout/login");
    }

    @Test(groups = "integration", description = "INT_02: Verify contact info update does not affect account balances")
    public void int02_updateDoesNotAffectBalances() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "INT_02");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        String accountId = overview.getAllAccountIds().get(0);
        double balanceBefore = overview.getBalanceForAccount(accountId);

        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();
        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Precondition: update should succeed");

        driver.navigate().to(driver.getCurrentUrl().replace("updateprofile.htm", "overview.htm"));
        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);
        double balanceAfter = refreshedOverview.getBalanceForAccount(accountId);

        Assert.assertEquals(balanceAfter, balanceBefore, 0.01,
                "Account balance should be completely unaffected by a contact info update");
    }

    @Test(groups = "integration", description = "INT_03: Verify updated contact info is unaffected by a subsequent Transfer Funds action")
    public void int03_updatePersistsAfterTransfer() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "INT_03");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Precondition: update should succeed");

        driver.navigate().to(driver.getCurrentUrl().replace("updateprofile.htm", "overview.htm"));
        AccountOverviewPage freshOverview = new AccountOverviewPage(driver);
        String fromAccount = freshOverview.getAllAccountIds().get(0);
        String toAccount = freshOverview.getAllAccountIds().size() > 1
                ? freshOverview.getAllAccountIds().get(1) : fromAccount;

        TransferFundsPage transferFundsPage = freshOverview.goToTransferFunds();
        transferFundsPage.transfer(data.get("TransferAmount"), fromAccount, toAccount);

        driver.navigate().to(driver.getCurrentUrl().replace("transfer.htm", "updateprofile.htm"));
        UpdateContactInfoPage reloadedContactPage = new UpdateContactInfoPage(driver);

        Assert.assertEquals(reloadedContactPage.getStreetValue(), data.get("Street"),
                "Contact info should remain unchanged after an unrelated Transfer Funds action");
    }

    @Test(groups = "integration", description = "INT_04: Verify updated contact info persists after navigating through Open New Account")
    public void int04_updatePersistsAfterNewAccountNavigation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "INT_04");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Precondition: update should succeed");

        driver.navigate().to(driver.getCurrentUrl().replace("updateprofile.htm", "openaccount.htm"));
        driver.navigate().to(driver.getCurrentUrl().replace("openaccount.htm", "updateprofile.htm"));
        UpdateContactInfoPage reloadedContactPage = new UpdateContactInfoPage(driver);

        Assert.assertEquals(reloadedContactPage.getCityValue(), data.get("City"),
                "Contact info should remain unchanged after navigating through Open New Account");
    }

    @Test(groups = "integration", description = "INT_05: Verify contact info remains updated after navigating to Request Loan and back")
    public void int05_updatePersistsAfterRequestLoanNavigation() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "INT_05");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Precondition: update should succeed");

        driver.navigate().to(driver.getCurrentUrl().replace("updateprofile.htm", "requestloan.htm"));
        RequestLoanPage loanPage = new RequestLoanPage(driver);
        Assert.assertNotNull(loanPage, "Should be able to navigate to Request Loan after a contact update");

        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "updateprofile.htm"));
        UpdateContactInfoPage reloadedContactPage = new UpdateContactInfoPage(driver);

        Assert.assertEquals(reloadedContactPage.getZipValue(), data.get("Zip"),
                "Contact info should remain unchanged after navigating through Request Loan");
    }

    // =========================================================================
    // END-TO-END (3) -- full multi-module user journeys.
    // =========================================================================

    @Test(groups = "e2e", description = "E2E_01: Login -> update contact info -> logout -> login -> verify persisted -> logout")
    public void e2e01_updatePersistenceJourney() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "E2E_01");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected update to succeed to proceed with the journey");

        LoginPage loginPage = new AccountOverviewPage(driver).logout();
        AccountOverviewPage reloginOverview = loginPage.login(USERNAME, PASSWORD);
        UpdateContactInfoPage reloadedContactPage = reloginOverview.goToUpdateContactInfo();

        Assert.assertEquals(reloadedContactPage.getStreetValue(), data.get("Street"),
                "Updated address should persist through the full logout/login journey");

        LoginPage finalLoginPage = reloginOverview.logout();
        Assert.assertNotNull(finalLoginPage, "Expected to land back on the login page after final logout");
    }

    @Test(groups = "e2e", description = "E2E_02: Login -> fail validation -> correct it -> resubmit successfully -> verify persisted -> logout")
    public void e2e02_correctValidationErrorJourney() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "E2E_02");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        // First attempt: leave city empty, expect failure.
        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .clearCity()
                .submitUpdate();
        Assert.assertFalse(contactPage.isUpdateSuccessful(), "First attempt with empty city should fail");

        // Correct the error and resubmit using RetryCity from the same row.
        driver.navigate().to(driver.getCurrentUrl());
        UpdateContactInfoPage retryPage = new UpdateContactInfoPage(driver);
        retryPage.updateAddress(data.get("Street"), data.get("RetryCity"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(retryPage.isUpdateSuccessful(), "Retry with all fields filled should succeed");

        LoginPage loginPage = new AccountOverviewPage(driver).logout();
        Assert.assertNotNull(loginPage, "Expected to land back on the login page after logout");
    }

    @Test(groups = "e2e", description = "E2E_03: Login -> update contact info -> transfer funds -> verify contact info unaffected -> logout")
    public void e2e03_updateThenTransferJourney() {
        Map<String, String> data = testData("UpdateContactInfoSuite", "E2E_03");
        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
                        data.get("Zip"), data.get("Phone"))
                .submitUpdate();
        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected update to succeed to proceed with the journey");

        driver.navigate().to(driver.getCurrentUrl().replace("updateprofile.htm", "overview.htm"));
        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);
        String fromAccount = refreshedOverview.getAllAccountIds().get(0);
        String toAccount = refreshedOverview.getAllAccountIds().size() > 1
                ? refreshedOverview.getAllAccountIds().get(1) : fromAccount;

        TransferFundsPage transferPage = refreshedOverview.goToTransferFunds();
        transferPage.transfer(data.get("TransferAmount"), fromAccount, toAccount);
        Assert.assertTrue(transferPage.isTransferSuccessful(), "Expected transfer to succeed as part of the journey");

        driver.navigate().to(driver.getCurrentUrl().replace("transfer.htm", "updateprofile.htm"));
        UpdateContactInfoPage reloadedContactPage = new UpdateContactInfoPage(driver);
        Assert.assertEquals(reloadedContactPage.getStreetValue(), data.get("Street"),
                "Contact info should remain unaffected after a Transfer Funds action later in the journey");

        LoginPage loginPage = new AccountOverviewPage(driver).logout();
        Assert.assertNotNull(loginPage, "Expected to land back on the login page after logout");
    }
}
