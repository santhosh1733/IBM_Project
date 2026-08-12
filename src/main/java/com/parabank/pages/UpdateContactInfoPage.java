package com.parabank.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public String getLastNameValue() {
        return lastNameInput.getAttribute("value");
    }

    public String getStreetValue() {
        return streetInput.getAttribute("value");
    }

    public String getCityValue() {
        return cityInput.getAttribute("value");
    }

    public String getStateValue() {
        return stateInput.getAttribute("value");
    }

    public String getZipValue() {
        return zipInput.getAttribute("value");
    }

    public String getPhoneValue() {
        return phoneInput.getAttribute("value");
    }

    /** Snapshot of every editable field, keyed by field name -- used for pre-fill and persistence checks. */
    public Map<String, String> getAllFieldValues() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("firstName", getFirstNameValue());
        values.put("lastName", getLastNameValue());
        values.put("street", getStreetValue());
        values.put("city", getCityValue());
        values.put("state", getStateValue());
        values.put("zip", getZipValue());
        values.put("phone", getPhoneValue());
        return values;
    }

    // Individual field clearers -- used by the mandatory-field validation
    // tests, so each one can leave exactly one field empty while keeping
    // the rest valid.
    public UpdateContactInfoPage clearStreet() {
        streetInput.clear();
        return this;
    }

    public UpdateContactInfoPage clearCity() {
        cityInput.clear();
        return this;
    }

    public UpdateContactInfoPage clearState() {
        stateInput.clear();
        return this;
    }

    public UpdateContactInfoPage clearZip() {
        zipInput.clear();
        return this;
    }

    public UpdateContactInfoPage clearPhone() {
        phoneInput.clear();
        return this;
    }
}
