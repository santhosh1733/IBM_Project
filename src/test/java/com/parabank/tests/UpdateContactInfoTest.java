package com.parabank.tests;

import com.parabank.pages.AccountOverviewPage;
import com.parabank.pages.LoginPage;
import com.parabank.pages.UpdateContactInfoPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateContactInfoTest extends BaseTest {

	   @Test(groups = "functional", description = "FUNC_01: Verify page pre-fills existing customer details correctly")
	    public void func01_prefillMatchesExistingData() {
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

	        Map<String, String> values = contactPage.getAllFieldValues();
	        Assert.assertFalse(values.get("firstName").isEmpty(), "First name should be pre-filled");
	        Assert.assertFalse(values.get("lastName").isEmpty(), "Last name should be pre-filled");
	        Assert.assertFalse(values.get("street").isEmpty(), "Street should be pre-filled");
	    }

	    @Test(groups = "functional", description = "FUNC_02: Verify successful update of all editable fields")
	    public void func02_allFieldsUpdateSuccessfully() {
	        Map<String, String> data = testData("UpdateContactInfoSuite", "FUNC_02");
	        AccountOverviewPage overview = new LoginPage(driver).login(USERNAME, PASSWORD);
	        UpdateContactInfoPage contactPage = overview.goToUpdateContactInfo();

	        contactPage.updateAddress(data.get("Street"), data.get("City"), data.get("State"),
	                        data.get("Zip"), data.get("Phone"))
	                .submitUpdate();
	        Assert.assertTrue(contactPage.isUpdateSuccessful(), "Expected all-field update to succeed");

	        driver.navigate().to(driver.getCurrentUrl());
	        UpdateContactInfoPage refreshedPage = new UpdateContactInfoPage(driver);
	        Assert.assertEquals(refreshedPage.getStreetValue(), data.get("Street"), "Street should reflect the update");
	        Assert.assertEquals(refreshedPage.getCityValue(), data.get("City"), "City should reflect the update");
	    }
}
