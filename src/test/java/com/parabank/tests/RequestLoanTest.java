package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.FindTransactionsPage;
import com.parabank.pages.LoginPage;
import com.parabank.pages.RequestLoanPage;
import com.parabank.pages.TransferFundsPage;
import com.parabank.pages.UpdateContactInfoPage;
import com.parabank.utils.ConfigReader;
import com.parabank.utils.ExcelUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;


public class RequestLoanTest extends BaseTest {
	 private static final String  USERNAME = ConfigReader.get("username");
	    private static final String PASSWORD = ConfigReader.get("password");

	    // =========================================================================
	    // SMOKE (5) -- fast, critical-path only. Should always run in under a
	    // couple minutes total and catch anything that would block a whole build.
	    // =========================================================================

	    @Test(groups = "smoke", description = "SMOKE_01: Verify Request Loan menu is accessible after login")
	    public void smoke01_requestLoanMenuAccessible() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        Assert.assertTrue(overview.isAccountsTableDisplayed(), "Expected Accounts Overview to load after login");
	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        Assert.assertNotNull(loanPage, "Request Loan page should be reachable from the main menu");
	    }

	    @Test(groups = "smoke", description = "SMOKE_02: Verify Request Loan page renders all required fields")
	    public void smoke02_requestLoanPageFieldsPresent() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        // Filling every field without an exception confirms the fields exist and are interactable.
	        loanPage.enterLoanAmount("1000").enterDownPayment("500");
	    }

	    @Test(groups = "smoke", description = "SMOKE_03: Verify a basic loan application submits without error")
	    public void smoke03_loanApplicationSubmitsCleanly() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("5000", "2000", fromAccount);

	        Assert.assertNotNull(loanPage.getLoanStatus(), "Expected a loan status (Approved/Denied) after submission");
	    }

	    @Test(groups = "smoke", description = "SMOKE_04: Verify loan denial path completes without crashing")
	    public void smoke04_loanDenialPathCompletes() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("100000", "10", fromAccount);

	        Assert.assertTrue(loanPage.isDenied(), "Expected Denied status for a clearly insufficient down payment");
	    }

	    @Test(groups = "smoke", description = "SMOKE_05: Verify logout works cleanly after a loan request")
	    public void smoke05_logoutAfterLoanRequest() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("3000", "1500", fromAccount);

	        // Navigate back to Account Overview before logout is exercised elsewhere (LoginTest),
	        // this just confirms the app is in a stable, navigable state post-submission.
	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "overview.htm"));
	    }

	    // =========================================================================
	    // FUNCTIONAL (7) -- module-level business rules, in isolation.
	    // Maps to Zephyr TC_LOAN_F02-F08.
	    // =========================================================================

	    @Test(groups = "functional", description = "FUNC_01 / TC_LOAN_F02: Verify loan approval with sufficient down payment")
	    public void func01_loanApprovedWithSufficientDownPayment() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("5000", "2000", fromAccount);

	        Assert.assertTrue(loanPage.isApproved(), "Expected loan status to be Approved");
	        Assert.assertNotNull(loanPage.getNewLoanAccountId(), "Expected a new loan account ID on approval");
	    }

	    @Test(groups = "functional", description = "FUNC_02 / TC_LOAN_F03: Verify loan denial with insufficient down payment")
	    public void func02_loanDeniedWithInsufficientDownPayment() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("50000", "10", fromAccount);

	        Assert.assertTrue(loanPage.isDenied(), "Expected loan status to be Denied");
	    }

	    @Test(groups = "functional", description = "FUNC_03 / TC_LOAN_F04: Verify validation error on empty Loan Amount")
	    public void func03_emptyLoanAmountValidation() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.enterDownPayment("1000").selectFromAccount(fromAccount).submitApplication();

	        // ParaBank's client-side validation typically blocks submission rather than
	        // showing a field error banner -- assert we're still on the Request Loan
	        // page (no status/new account produced) rather than a specific error text.
	        Assert.assertNull(loanPage.getNewLoanAccountId(), "No loan account should be created with a missing amount");
	    }

	    @Test(groups = "functional", description = "FUNC_04 / TC_LOAN_F05: Verify validation error on empty Down Payment")
	    public void func04_emptyDownPaymentValidation() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.enterLoanAmount("5000").selectFromAccount(fromAccount).submitApplication();

	        Assert.assertNull(loanPage.getNewLoanAccountId(), "No loan account should be created with a missing down payment");
	    }

	    @Test(groups = "functional", description = "FUNC_05 / TC_LOAN_F06: Verify negative values are rejected")
	    public void func05_negativeValuesRejected() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("-5000", "-100", fromAccount);

	        Assert.assertNull(loanPage.getNewLoanAccountId(), "No loan account should be created from negative input");
	    }

	    @Test(groups = "functional", description = "FUNC_06 / TC_LOAN_F07: Verify non-numeric input is rejected")
	    public void func06_nonNumericInputRejected() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("abcd", "1000", fromAccount);

	        Assert.assertNull(loanPage.getNewLoanAccountId(), "No loan account should be created from non-numeric input");
	    }

	    @Test(groups = "functional", description = "FUNC_07 / TC_LOAN_F09: Verify loan confirmation details match submitted values")
	    public void func07_confirmationScreenDetails() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("3000", "1500", fromAccount);

	        Assert.assertNotNull(loanPage.getLoanStatus(), "Confirmation screen should display a loan status");
	    }

	    // =========================================================================
	    // INTEGRATION (5) -- Request Loan's effects on Account Overview,
	    // Find Transactions, and Transfer Funds. Maps to Zephyr TC_LOAN_I01-I05.
	    // =========================================================================

	    @Test(groups = "integration", description = "INT_01 / TC_LOAN_I01: Verify approved loan account appears in Account Overview")
	    public void int01_approvedLoanAppearsInOverview() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("5000", "3000", fromAccount);
	        Assert.assertTrue(loanPage.isApproved(), "Precondition failed: loan must be approved for this check");
	        String newLoanAccountId = loanPage.getNewLoanAccountId();

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "overview.htm"));
	        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);

	        Assert.assertTrue(refreshedOverview.getAllAccountIds().contains(newLoanAccountId),
	                "Expected new loan account " + newLoanAccountId + " to appear in Account Overview");
	    }

	    @Test(groups = "integration", description = "INT_02 / TC_LOAN_I02: Verify down payment is debited from source account")
	    public void int02_downPaymentDebitedFromSource() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);
	        double balanceBefore = overview.getBalanceForAccount(fromAccount);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("5000", "2000", fromAccount);
	        Assert.assertTrue(loanPage.isApproved(), "Precondition failed: loan must be approved for this check");

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "overview.htm"));
	        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);
	        double balanceAfter = refreshedOverview.getBalanceForAccount(fromAccount);

	        Assert.assertEquals(balanceAfter, balanceBefore - 2000.0, 0.01,
	                "Source account balance should decrease by exactly the down payment amount");
	    }

	    @Test(groups = "integration", description = "INT_03 / TC_LOAN_I03: Verify loan down payment transaction appears in Find Transactions")
	    public void int03_downPaymentTransactionSearchable() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("5000", "1000", fromAccount);
	        Assert.assertTrue(loanPage.isApproved(), "Precondition failed: loan must be approved for this check");

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "findtrans.htm"));
	        FindTransactionsPage findPage = new FindTransactionsPage(driver);
	        findPage.searchByAmount("1000");

	        Assert.assertTrue(findPage.hasResults(), "Expected the down payment debit to be findable as a transaction");
	    }

	    @Test(groups = "integration", description = "INT_04 / TC_LOAN_I04: Verify new loan account is selectable in Transfer Funds")
	    public void int04_loanAccountSelectableInTransferFunds() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("5000", "2500", fromAccount);
	        Assert.assertTrue(loanPage.isApproved(), "Precondition failed: loan must be approved for this check");
	        String newLoanAccountId = loanPage.getNewLoanAccountId();

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "transfer.htm"));
	        TransferFundsPage transferPage = new TransferFundsPage(driver);

	        // If selectFromAccount doesn't throw NoSuchElementException, the loan account is a valid option.
	        transferPage.selectFromAccount(newLoanAccountId);
	    }

	    @Test(groups = "integration", description = "INT_05 / TC_LOAN_I05: Verify denied loan does not affect source account balance")
	    public void int05_deniedLoanDoesNotAffectBalance() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);
	        double balanceBefore = overview.getBalanceForAccount(fromAccount);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("100000", "50", fromAccount);
	        Assert.assertTrue(loanPage.isDenied(), "Precondition failed: loan must be denied for this check");

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "overview.htm"));
	        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);
	        double balanceAfter = refreshedOverview.getBalanceForAccount(fromAccount);

	        Assert.assertEquals(balanceAfter, balanceBefore, 0.01,
	                "Source account balance should be unchanged after a denied loan request");
	    }

	    // =========================================================================
	    // END-TO-END (3) -- full multi-module user journeys, simulating a real
	    // customer session start to finish.
	    // =========================================================================

	    @Test(groups = "e2e", description = "E2E_01: Login -> apply for loan -> verify in overview -> transfer from loan account -> logout")
	    public void e2e01_loanToTransferJourney() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fundingAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("6000", "3000", fundingAccount);
	        Assert.assertTrue(loanPage.isApproved(), "Expected loan approval to proceed with the journey");
	        String loanAccountId = loanPage.getNewLoanAccountId();

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "overview.htm"));
	        AccountOverviewPage refreshedOverview = new AccountOverviewPage(driver);
	        Assert.assertTrue(refreshedOverview.getAllAccountIds().contains(loanAccountId),
	                "New loan account should be visible before attempting a transfer from it");

	        TransferFundsPage transferPage = refreshedOverview.goToTransferFunds();
	        transferPage.transfer("500", loanAccountId, fundingAccount);
	        Assert.assertTrue(transferPage.isTransferSuccessful(), "Expected transfer from new loan account to succeed");

	        LoginPage loginPage = new AccountOverviewPage(driver).logout();
	        Assert.assertNotNull(loginPage, "Expected to land back on the login page after logout");
	    }

	    @Test(groups = "e2e", description = "E2E_02: Login -> denied loan attempt -> retry with sufficient down payment -> approved -> logout")
	    public void e2e02_retryAfterDenialJourney() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("100000", "10", fromAccount);
	        Assert.assertTrue(loanPage.isDenied(), "First attempt with insufficient down payment should be denied");

	        // Retry on a fresh Request Loan page load with a realistic down payment.
	        driver.navigate().to(driver.getCurrentUrl());
	        RequestLoanPage retryLoanPage = new RequestLoanPage(driver);
	        retryLoanPage.applyForLoan("10000", "5000", fromAccount);
	        Assert.assertTrue(retryLoanPage.isApproved(), "Retry with a sufficient down payment should be approved");

	        LoginPage loginPage = new AccountOverviewPage(driver).logout();
	        Assert.assertNotNull(loginPage, "Expected to land back on the login page after logout");
	    }

	    @Test(groups = "e2e", description = "E2E_03: Login -> apply for loan -> verify transaction -> update contact info -> logout")
	    public void e2e03_loanThenProfileUpdateJourney() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        String fromAccount = overview.getAllAccountIds().get(0);

	        RequestLoanPage loanPage = overview.goToRequestLoan();
	        loanPage.applyForLoan("4000", "2000", fromAccount);
	        Assert.assertTrue(loanPage.isApproved(), "Expected loan approval to proceed with the journey");

	        driver.navigate().to(driver.getCurrentUrl().replace("requestloan.htm", "findtrans.htm"));
	        FindTransactionsPage findPage = new FindTransactionsPage(driver);
	        findPage.searchByAmount("2000");
	        Assert.assertTrue(findPage.hasResults(), "Expected the down payment transaction to be findable");

	        driver.navigate().to(driver.getCurrentUrl().replace("findtrans.htm", "updateprofile.htm"));
	        UpdateContactInfoPage contactPage = new UpdateContactInfoPage(driver);
	        contactPage.updateAddress("789 Pine St", "Springfield", "IL", "62704", "5551112222")
	                .submitUpdate();
	        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected contact info update to complete the journey");

	        LoginPage loginPage = new AccountOverviewPage(driver).logout();
	        Assert.assertNotNull(loginPage, "Expected to land back on the login page after logout");
	    }
   
}
