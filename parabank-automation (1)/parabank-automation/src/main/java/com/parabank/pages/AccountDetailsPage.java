package com.parabank.pages;

import com.parabank.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Account Details / Account Activity page used by M4 transaction-history
 * verification.
 *
 * The transaction-table locator supports the known ParaBank table ids and a
 * semantic header-based fallback, so M4 does not depend on one guessed id.
 */
public class AccountDetailsPage extends BasePage {

    private static final String TRANSACTION_TABLE_XPATH =
            "//*[@id='transactionTable' or @id='transactionsTable']" +
            " | //table[.//th[contains(normalize-space(.),'Transaction')]" +
            " and .//th[contains(normalize-space(.),'Debit')]]";

    private static final By TRANSACTION_TABLE = By.xpath(TRANSACTION_TABLE_XPATH);

    @FindBy(id = "accountId")
    private WebElement accountIdValue;

    @FindBy(xpath = TRANSACTION_TABLE_XPATH)
    private WebElement transactionTable;

    public AccountDetailsPage(WebDriver driver) {
        super(driver);
    }

    public AccountDetailsPage waitUntilLoaded() {
        WaitUtils.waitForVisible(driver, TRANSACTION_TABLE);
        return this;
    }

    public String getAccountId() {
        return getText(accountIdValue);
    }

    public boolean isTransactionTableDisplayed() {
        return isDisplayed(transactionTable);
    }

    /**
     * Waits for a transaction row containing the payee and then verifies the
     * debit amount in that same row. This avoids coupling the test to fixed row
     * numbers because ParaBank inserts new transactions dynamically.
     */
    public boolean hasBillPaymentTransaction(String payeeName, String expectedAmount) {
        String payeeLiteral = toXPathLiteral(payeeName);
        By transactionRow = By.xpath(
                "(" + TRANSACTION_TABLE_XPATH + ")" +
                "//tbody/tr[contains(normalize-space(.), " + payeeLiteral + ")]"
        );

        try {
            WebElement row = WaitUtils.waitForVisible(driver, transactionRow);
            String rowText = row.getText().replace(",", "").trim();
            String normalizedAmount = normalizeAmount(expectedAmount);
            return rowText.contains(payeeName) && rowText.contains(normalizedAmount);
        } catch (TimeoutException e) {
            return false;
        }
    }

    private String normalizeAmount(String amount) {
        String value = amount == null ? "" : amount.replace("$", "").replace(",", "").trim();
        try {
            return String.format(java.util.Locale.US, "%.2f", Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private String toXPathLiteral(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        }
        if (!text.contains("\"")) {
            return "\"" + text + "\"";
        }

        String[] parts = text.split("'", -1);
        StringBuilder xpath = new StringBuilder("concat(");
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                xpath.append(", \"'\", ");
            }
            xpath.append("'").append(parts[i]).append("'");
        }
        xpath.append(")");
        return xpath.toString();
    }
}
