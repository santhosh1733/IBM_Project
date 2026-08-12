package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Find Transactions page object (Owner: M3).
 * STARTER SCAFFOLD -- ParaBank exposes several search modes (by ID, by date,
 * by date range, by amount) as separate collapsible panels/tabs. Verify each
 * panel's field IDs individually; they differ per search type.
 */
public class FindTransactionsPage extends BasePage {

    @FindBy(id = "transactionId")
    private WebElement transactionIdInput;

    @FindBy(id = "findById")
    private WebElement findByIdButton;

    @FindBy(id = "amount")
    private WebElement amountInput;

    @FindBy(id = "findByAmount")
    private WebElement findByAmountButton;

    @FindBy(css = "#transactionsTable tbody tr")
    private List<WebElement> resultRows;

    @FindBy(css = "#transactionsTable")
    private WebElement resultsTable;

    public FindTransactionsPage(WebDriver driver) {
        super(driver);
    }

    public FindTransactionsPage searchByTransactionId(String transactionId) {
        type(transactionIdInput, transactionId);
        click(findByIdButton);
        return this;
    }

    public FindTransactionsPage searchByAmount(String amount) {
        type(amountInput, amount);
        click(findByAmountButton);
        return this;
    }

    public int getResultCount() {
        return resultRows.size();
    }

    public boolean hasResults() {
        return isDisplayed(resultsTable) && !resultRows.isEmpty();
    }

    // TODO (M3): add searchByDate() and searchByDateRange() once you've inspected
    // ParaBank's date-picker panel IDs, and a getNoResultsMessage() for the empty state.
}