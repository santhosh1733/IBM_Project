package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Update Contact Info page object (Owner: M5).
 * STARTER SCAFFOLD -- verify field names/ids against the live DOM.
 */
public class UpdateContactInfoPage extends BasePage {

    @FindBy(id = "customer.firstName")
    private WebElement firstNameInput;

    @FindBy(id = "customer.lastName")
    private WebElement lastNameInput;

    @FindBy(id = "customer.address.street")
    private WebElement streetInput;

    @FindBy(id = "customer.address.city")
    private WebElement cityInput;

    @FindBy(id = "customer.address.state")
    private WebElement stateInput;

    @FindBy(id = "customer.address.zipCode")
    private WebElement zipInput;

    @FindBy(id = "customer.phoneNumber")
    private WebElement phoneInput;

    @FindBy(css = "input.button[value='Update Profile']")
    private WebElement updateProfileButton;

    @FindBy(css = "#updateProfileResult h1")
    private WebElement confirmationHeading;

    public UpdateContactInfoPage(WebDriver driver) {
        super(driver);
    }

    public UpdateContactInfoPage updateAddress(String street, String city, String state,
                                                String zip, String phone) {
        type(streetInput, street);
        type(cityInput, city);
        type(stateInput, state);
        type(zipInput, zip);
        type(phoneInput, phone);
        return this;
    }

    public UpdateContactInfoPage submitUpdate() {
        click(updateProfileButton);
        return this;
    }

    public boolean isUpdateSuccessful() {
        return isDisplayed(confirmationHeading);
    }

    public String getFirstNameValue() {
        return firstNameInput.getAttribute("value");
    }

    // TODO (M5): add a getAllFieldValues() helper (returns a Map<String,String>)
    // for the "pre-fill matches existing data" test case.
}
