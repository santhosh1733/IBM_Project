package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * Bill Pay page object (Owner: M4).
 * STARTER SCAFFOLD -- verify locators against the live DOM before relying on
 * them. ParaBank's Bill Pay form fields use name="payee.<field>" and
 * name="amount" -- confirm exact attribute names via Inspect.
 */
public class BillPayPage extends BasePage {

    @FindBy(name = "payee.name")
    private WebElement payeeNameInput;

    @FindBy(name = "payee.address.street")
    private WebElement payeeStreetInput;

    @FindBy(name = "payee.address.city")
    private WebElement payeeCityInput;

    @FindBy(name = "payee.address.state")
    private WebElement payeeStateInput;

    @FindBy(name = "payee.address.zipCode")
    private WebElement payeeZipInput;

    @FindBy(name = "payee.phoneNumber")
    private WebElement payeePhoneInput;

    @FindBy(name = "payee.accountNumber")
    private WebElement payeeAccountInput;

    @FindBy(name = "verifyAccount")
    private WebElement verifyAccountInput;

    @FindBy(name = "amount")
    private WebElement amountInput;

    @FindBy(id = "fromAccountId")
    private WebElement fromAccountDropdown;

    @FindBy(css = "input.button[value='Send Payment']")
    private WebElement sendPaymentButton;

    @FindBy(css = "#billpayResult h1")
    private WebElement confirmationHeading;

    public BillPayPage(WebDriver driver) {
        super(driver);
    }

    public BillPayPage fillPayeeDetails(String name, String street, String city, String state,
                                         String zip, String phone, String accountNumber) {
        type(payeeNameInput, name);
        type(payeeStreetInput, street);
        type(payeeCityInput, city);
        type(payeeStateInput, state);
        type(payeeZipInput, zip);
        type(payeePhoneInput, phone);
        type(payeeAccountInput, accountNumber);
        type(verifyAccountInput, accountNumber);
        return this;
    }

    public BillPayPage enterAmount(String amount) {
        type(amountInput, amount);
        return this;
    }

    public BillPayPage selectFromAccount(String accountId) {
        new Select(fromAccountDropdown).selectByVisibleText(accountId);
        return this;
    }

    public BillPayPage submitPayment() {
        click(sendPaymentButton);
        return this;
    }

    public boolean isPaymentSuccessful() {
        return isDisplayed(confirmationHeading) && getText(confirmationHeading).contains("Complete");
    }

    // TODO (M4): add field-level validation-error getters once you've inspected
    // how ParaBank renders "X is required" messages for each mandatory field.
}
