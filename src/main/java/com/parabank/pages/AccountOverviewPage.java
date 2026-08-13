package com.parabank.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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
	
	@FindBy(css = "h1.title")
	private WebElement accountActivityTitle;
	
	@FindBy(css = "#accountTable tfoot tr td:nth-child(2)")
	private WebElement totalBalance;
	
	@FindBy(css = "#transactionTable")
	private WebElement transactionTable;

	@FindBy(css = "#transactionTable tbody tr")
	private List<WebElement> transactionRows;
	
	public String getTotalBalance() {
	    return totalBalance.getText();
	}

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
		WebElement balanceCell = driver.findElement(By.xpath("//a[text()='" + accountId + "']/../../td[2]"));
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

	public boolean isAccountOverviewPageDisplayed() {
		return accountsTable.isDisplayed();
	}

	// Account Activity page element

	public void clickAccount(String accountId) {

		WebElement accountLink = driver
				.findElement(By.xpath("//table[@id='accountTable']//a[text()='" + accountId + "']"));

		click(accountLink);
	}

	public boolean isAccountActivityPageDisplayed() {
		return accountActivityTitle.isDisplayed();
	}
	public int getAccountCount() {
	    return accountIdLinks.size();
	}
	public String getFirstAccountNumber() {
	    return accountIdLinks.get(0).getText();
	}

	public String getSecondAccountNumber() {
	    return accountIdLinks.get(1).getText();
	}
	
	public double getAccountBalance(String accountId) {

	    WebElement balanceElement = driver.findElement(
	        By.xpath("//table[@id='accountTable']//tr[td[1]//a[text()='"
	                + accountId + "']]/td[2]")
	    );

	    String balanceText = balanceElement.getText();

	    return Double.parseDouble(
	        balanceText.replace("$", "").replace(",", "").trim()
	    );
	}
	public boolean isTransactionTableDisplayed() {

	    return isDisplayed(transactionTable);
	}
	public boolean isTransactionDisplayed() {

	    return transactionRows.size() > 0;
	}
	public List<String> getTransactionDetails() {

	    return transactionRows.stream()
	            .map(WebElement::getText)
	            .collect(Collectors.toList());
	}
	

	public LoginPage logout() {
		click(logoutLink);
		return new LoginPage(driver);
	}}
