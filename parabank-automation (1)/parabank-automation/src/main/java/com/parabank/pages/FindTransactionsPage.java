package com.parabank.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FindTransactionsPage extends BasePage {

	private WebDriverWait wait;

	// Transaction ID search
	@FindBy(id = "transactionId")
	private WebElement transactionIdInput;

	@FindBy(id = "findById")
	private WebElement findByIdButton;

	// Amount search
	@FindBy(id = "amount")
	private WebElement amountInput;

	@FindBy(id = "findByAmount")
	private WebElement findByAmountButton;

	// Date search
	@FindBy(id = "transactionDate")
	private WebElement dateField;

	@FindBy(id = "findByDate")
	private WebElement findByDateButton;

	// Search result
	@FindBy(id = "transactionTable")
	private WebElement resultsTable;

	@FindBy(css = "#transactionBody tr")
	private List<WebElement> resultRows;

	// Date Range search
	@FindBy(id = "fromDate")
	private WebElement fromDateField;

	@FindBy(id = "toDate")
	private WebElement toDateField;

	@FindBy(id = "findByDateRange")
	private WebElement findByDateRangeButton;

	public FindTransactionsPage(WebDriver driver) {
		super(driver);
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}

	public FindTransactionsPage searchByTransactionId(String transactionId) {

		wait.until(ExpectedConditions.visibilityOf(transactionIdInput));
		transactionIdInput.clear();
		transactionIdInput.sendKeys(transactionId);
		System.out.println("Entered Transaction ID: " + transactionIdInput.getAttribute("value"));
		wait.until(ExpectedConditions.elementToBeClickable(findByIdButton));
		findByIdButton.click();

		// Wait for the Find Transactions page to load
		wait.until(ExpectedConditions.urlContains("findtrans.htm"));
		System.out.println("URL after search: " + driver.getCurrentUrl());

		// Print the complete visible page text for debugging
		String pageText = driver.findElement(org.openqa.selenium.By.tagName("body")).getText();
		System.out.println("========== PAGE TEXT AFTER SEARCH ==========");
		System.out.println(pageText);
		System.out.println("============================================");

		return this;
	}

	// Search by Amount
	public FindTransactionsPage searchByAmount(String amount) throws InterruptedException {

		wait.until(ExpectedConditions.visibilityOf(amountInput));
		amountInput.clear();
		amountInput.sendKeys(amount);
		System.out.println("Entered Amount: " + amountInput.getAttribute("value"));
		System.out.println("Amount button displayed: " + findByAmountButton.isDisplayed());
		System.out.println("Amount button enabled: " + findByAmountButton.isEnabled());
		System.out.println("Amount button type: " + findByAmountButton.getAttribute("type"));
		System.out.println("Amount button value: " + findByAmountButton.getAttribute("value"));
		System.out.println("Amount input name: " + amountInput.getAttribute("name"));
		System.out.println("Amount input id: " + amountInput.getAttribute("id"));
		wait.until(ExpectedConditions.elementToBeClickable(findByAmountButton));
		findByAmountButton.click();
		Thread.sleep(1000);
		System.out.println("URL after amount search: " + driver.getCurrentUrl());
		String pageText = driver.findElement(org.openqa.selenium.By.tagName("body")).getText();
		System.out.println("========== PAGE TEXT AFTER AMOUNT SEARCH ==========");
		System.out.println(pageText);
		System.out.println("==================================================");
		return this;
	}

	// Search by Date
	public FindTransactionsPage searchByDate(String date) {

		wait.until(ExpectedConditions.visibilityOf(dateField));
		dateField.clear();
		dateField.sendKeys(date);
		// Click Find Transactions
		wait.until(ExpectedConditions.elementToBeClickable(findByDateButton));
		findByDateButton.click();
		return this;
	}

	// Search by Date Range
	public FindTransactionsPage searchByDateRange(String fromDate, String toDate) {

		wait.until(ExpectedConditions.visibilityOf(fromDateField));
		fromDateField.clear();
		fromDateField.sendKeys(fromDate);
		wait.until(ExpectedConditions.visibilityOf(toDateField));
		toDateField.clear();
		toDateField.sendKeys(toDate);
		// Click Find Transactions
		wait.until(ExpectedConditions.elementToBeClickable(findByDateRangeButton));
		findByDateRangeButton.click();
		return this;
	}

	// Check whether results are displayed
	public boolean hasResults() {

		System.out.println("Results table displayed: " + isDisplayed(resultsTable));
		System.out.println("Number of result rows: " + resultRows.size());
		return isDisplayed(resultsTable) && !resultRows.isEmpty();
	}

	// Get number of results
	public int getResultCount() {
		try {
			wait.until(ExpectedConditions.visibilityOf(resultsTable));
			return resultRows.size();
		} catch (Exception e) {
			return 0;
		}
	}

	// Verify actual Transaction ID is present
	public boolean isTransactionIdDisplayed(String transactionId) {
		try {
			wait.until(ExpectedConditions.visibilityOf(resultsTable));
			for (WebElement row : resultRows) {
				if (row.getText().contains(transactionId)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	// Verify all displayed transactions contain the searched amount
	public boolean areAllResultsForAmountDisplayed(String amount) {
		try {
			wait.until(ExpectedConditions.visibilityOf(resultsTable));
			if (resultRows.isEmpty()) {
				return false;
			}
			for (WebElement row : resultRows) {
				if (!row.getText().contains(amount)) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// Verify transaction description is displayed
	public boolean isTransactionDescriptionDisplayed(String description) {
		try {
			wait.until(ExpectedConditions.visibilityOf(resultsTable));
			for (WebElement row : resultRows) {
				if (row.getText().contains(description)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	// Verify transaction amount is displayed
	public boolean isTransactionAmountDisplayed(String amount) {
		try {
			wait.until(ExpectedConditions.visibilityOf(resultsTable));
			for (WebElement row : resultRows) {
				if (row.getText().contains(amount)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	// Click transaction from search results
	public void clickFirstTransaction() {
		wait.until(ExpectedConditions.visibilityOf(resultsTable));
		wait.until(ExpectedConditions.elementToBeClickable(resultRows.get(0).findElement(By.tagName("a"))));
		resultRows.get(0).findElement(By.tagName("a")).click();
	}

//Verify transaction details page is displayed
	public boolean isTransactionDetailsPageDisplayed() {
		try {
			wait.until(ExpectedConditions.urlContains("transaction.htm"));
			return driver.getCurrentUrl().contains("transaction.htm");
		} catch (Exception e) {
			return false;
		}
	}
}
