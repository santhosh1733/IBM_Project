package com.parabank.pages;

import com.parabank.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.Locale;

/**
 * Bill Pay specific Find Transactions page object.
 *
 * ParaBank searches transactions for one account at a time.
 * PB_AUTO_07 explicitly selects the SAME source account that was used
 * for the Bill Payment before searching by amount.
 */
public class BillPayFindTransactionsPage extends BasePage {

    @FindBy(id = "accountId")
    private WebElement accountDropdown;

    @FindBy(id = "amount")
    private WebElement amountInput;

    @FindBy(id = "findByAmount")
    private WebElement findByAmountButton;

    private static final String RESULTS_TABLE_XPATH =
            "//*[@id='transactionTable' or @id='transactionsTable']"
            + " | //table[.//th[contains(normalize-space(.),'Transaction')]"
            + " and .//th[contains(normalize-space(.),'Debit')]]";

    private static final By RESULTS_TABLE = By.xpath(RESULTS_TABLE_XPATH);

    public BillPayFindTransactionsPage(WebDriver driver) {
        super(driver);
    }

    public BillPayFindTransactionsPage waitUntilLoaded() {
        WaitUtils.waitForVisible(driver, accountDropdown);
        WaitUtils.waitForVisible(driver, amountInput);
        WaitUtils.waitForClickable(driver, findByAmountButton);
        return this;
    }

    public BillPayFindTransactionsPage selectAccount(String accountId) {
        WaitUtils.waitForVisible(driver, accountDropdown);
        new Select(accountDropdown).selectByVisibleText(accountId);
        return this;
    }

    public String getSelectedAccountId() {
        WaitUtils.waitForVisible(driver, accountDropdown);
        return new Select(accountDropdown)
                .getFirstSelectedOption()
                .getText()
                .trim();
    }

    public BillPayFindTransactionsPage searchByAmount(String amount) {
        type(amountInput, amount);
        click(findByAmountButton);
        return this;
    }

    public BillPayFindTransactionsPage searchByAmount(String accountId, String amount) {
        selectAccount(accountId);
        return searchByAmount(amount);
    }

    public boolean hasTransactionContaining(String payeeName, String expectedAmount) {
        String normalizedPayee = normalizeText(payeeName);
        String normalizedAmount = normalizeAmount(expectedAmount);

        String rowTextLower =
                "translate(normalize-space(.),"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz')";

        By matchingRow = By.xpath(
                "(" + RESULTS_TABLE_XPATH + ")"
                + "//tbody/tr[contains(" + rowTextLower + ", "
                + toXPathLiteral(normalizedPayee) + ")]"
        );

        try {
            WaitUtils.waitForVisible(driver, RESULTS_TABLE);
            WebElement row = WaitUtils.waitForVisible(driver, matchingRow);

            String rowText = normalizeText(row.getText())
                    .replace(",", "");

            return rowText.contains(normalizedPayee)
                    && rowText.contains(normalizedAmount);
        } catch (TimeoutException e) {
            return false;
        }
    }

    private String normalizeText(String text) {
        return text == null
                ? ""
                : text.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeAmount(String amount) {
        String value = amount == null
                ? ""
                : amount.replace("$", "").replace(",", "").trim();

        try {
            return String.format(Locale.US, "%.2f", Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return value.toLowerCase(Locale.ROOT);
        }
    }

    private String toXPathLiteral(String text) {
        if (text == null) {
            return "''";
        }
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
