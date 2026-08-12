package com.parabank.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.parabank.utils.WaitUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Account Overview / Accounts Overview page (Owner: M1 - after login this is
 * the landing page, so shared navigation links live here too).
 */
public class AccountOverviewPage extends BasePage {

    @FindBy(css = "#accountTable")
    private WebElement accountsTable;

    @FindBy(css = "#accountTable tbody tr td:first-child a")
    private List<WebElement> accountIdLinks;

    @FindBy(linkText = "Open New Account")
    private WebElement openNewAccountLink;

    @FindBy(linkText = "Transfer Funds")
    private WebElement transferFundsLink;

    @FindBy(linkText = "Bill Pay")
    private WebElement billPayLink;

    @FindBy(linkText = "Find Transactions")
    private WebElement findTransactionsLink;

    @FindBy(linkText = "Update Contact Info")
    private WebElement updateContactInfoLink;

    @FindBy(linkText = "Request Loan")
    private WebElement requestLoanLink;

    @FindBy(linkText = "Log Out")
    private WebElement logoutLink;

    public AccountOverviewPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAccountsTableDisplayed() {
        return isDisplayed(accountsTable);
    }

    /** Returns all account IDs currently listed on the Overview page. */
    public List<String> getAllAccountIds() {
        return accountIdLinks.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public double getBalanceForAccount(String accountId) {
        WebElement balanceCell = driver.findElement(
                By.xpath("//a[text()='" + accountId + "']/../../td[2]"));
        String raw = balanceCell.getText().replace("$", "").replace(",", "").trim();
        return Double.parseDouble(raw);
    }

    public RequestLoanPage goToRequestLoan() {
        click(requestLoanLink);
        return new RequestLoanPage(driver);
    }

    public TransferFundsPage goToTransferFunds() {
        click(transferFundsLink);
        return new TransferFundsPage(driver);
    }

    public BillPayPage goToBillPay() {
        click(billPayLink);
        return new BillPayPage(driver);
    }

    public FindTransactionsPage goToFindTransactions() {
        click(findTransactionsLink);
        return new FindTransactionsPage(driver);
    }

    public UpdateContactInfoPage goToUpdateContactInfo() {
        click(updateContactInfoLink);
        return new UpdateContactInfoPage(driver);
    }

    public OpenNewAccountPage goToOpenNewAccount() {
        click(openNewAccountLink);
        return new OpenNewAccountPage(driver);
    }

    public LoginPage logout() {
        click(logoutLink);
        return new LoginPage(driver);
    }

	public void viewAccountDetails(String fromAccount) {
		    String xpath = "//a[normalize-space(text())='" + fromAccount + "']";

		    WebElement accountLink = driver.findElement(By.xpath(xpath));

		    WaitUtils.waitForClickable(driver, By.xpath(xpath));
		    accountLink.click();
		
	}

	public boolean isTransactionInHistory(String transferAmountStr) {
	    By transactionRows = By.cssSelector("#transactionTable tbody tr");

	    try {
	        List<WebElement> rows = driver.findElements(transactionRows);

	        for (WebElement row : rows) {
	            String rowText = row.getText().trim();

	            if (rowText.contains(transferAmountStr)) {
	                return true;
	            }
	        }

	    } catch (Exception e) {
	        return false;
	    }

	    return false;
	}
}
