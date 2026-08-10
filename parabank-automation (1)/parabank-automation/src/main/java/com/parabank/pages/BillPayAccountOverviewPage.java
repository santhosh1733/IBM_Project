package com.parabank.pages;

import com.parabank.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * M4-only extension around the shared AccountOverviewPage.
 *
 * This keeps Bill Pay's extra navigation helpers out of the shared M1/M2 page
 * while still reusing the existing account table and balance methods.
 */
public class BillPayAccountOverviewPage extends AccountOverviewPage {

    private static final By FIRST_ACCOUNT_LINK =
            By.cssSelector("#accountTable tbody tr td:first-child a");

    public BillPayAccountOverviewPage(WebDriver driver) {
        super(driver);
    }

    public BillPayAccountOverviewPage waitUntilLoaded() {
        WaitUtils.waitForVisible(driver, By.id("accountTable"));
        WaitUtils.waitForVisible(driver, FIRST_ACCOUNT_LINK);
        return this;
    }

    public String getFirstAccountId() {
        waitUntilLoaded();
        List<String> accountIds = getAllAccountIds();
        if (accountIds.isEmpty()) {
            throw new IllegalStateException("No active ParaBank account is available for Bill Pay testing.");
        }
        return accountIds.get(0);
    }

    public double getCurrentBalance(String accountId) {
        WaitUtils.waitForVisible(driver,
                By.xpath("//a[normalize-space()='" + accountId + "']/ancestor::tr/td[2]"));
        return getBalanceForAccount(accountId);
    }

    public AccountDetailsPage openAccount(String accountId) {
        WebElement accountLink = WaitUtils.waitForClickable(driver,
                By.xpath("//a[normalize-space()='" + accountId + "']"));
        click(accountLink);
        return new AccountDetailsPage(driver).waitUntilLoaded();
    }
}
