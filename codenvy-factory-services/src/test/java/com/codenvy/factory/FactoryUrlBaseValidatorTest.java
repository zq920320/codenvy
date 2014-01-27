/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.factory;

import com.codenvy.api.factory.*;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.exception.AccountExistenceException;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Account;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryUrlBaseValidatorTest {
    private static String VALID_REPOSITORY_URL = "http://github.com/codenvy/cloudide";

    private static final String ID = "id";

    @Mock
    private AccountManager accountManager;

    @Mock
    private Account account;

    @InjectMocks
    private FactoryUrlBaseValidator validator;

    private SimpleFactoryUrl url;

    private AdvancedFactoryUrl advUrl;

    @BeforeMethod
    public void setUp() {
        url = new SimpleFactoryUrl("1.0", "git", VALID_REPOSITORY_URL, null, null, null, false, null, null, null, null, null);

        advUrl = new AdvancedFactoryUrl("1.1", "git", VALID_REPOSITORY_URL, "123456798", null, null, false, null, null, null, null, null);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountDoesNotExist() throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        doThrow(new AccountExistenceException()).when(accountManager).getAccountById(ID);

        // when, then
        validator.validateUrl(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountHasNoCertainProperty() throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(null);

        // when, then
        validator.validateUrl(url);
    }

    @Test(dataProvider = "illegalFormatPropertyValue", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfTariffEndTimePropertyHasIllegalFormat(String propertyValue)
            throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(propertyValue);

        // when, then
        validator.validateUrl(url);
    }

    @DataProvider(name = "illegalFormatPropertyValue")
    public Object[][] illegalFormatProvider() {
        return new Object[][]{
                {"smth"},
                {"11.15.2051"},
                {"11.15.2051 15:45"},
                {"11.15.2051 15:45:45"},
                {"15.11.2051"},
                {"15.11.2051 15:45"},
                {"15.11.2051 15:45:45"},
                {"11-15-2051"},
                {"15-11-2051"},
                {"11-15-2051 15:54"},
                {"15-11-2051 15:54"},
                {"15-11-2051 15:45:45"}
        };
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfOrgIdExpires() throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn("2012-11-30 11:21:15");

        // when, then
        validator.validateUrl(url);
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdIsValid() throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn("2022-11-30 11:21:15");

        // when, then
        validator.validateUrl(url);
    }

    @Test(dataProvider = "validProjectNamesProvider")
    public void shouldBeAbleToValidateValidProjectName(String projectName) throws Exception {
        Map<String, String> projectAttributes = new HashMap<>();
        projectAttributes.put("pname", projectName);
        url.setProjectattributes(projectAttributes);
        validator.validateUrl(url);
    }

    @Test(dataProvider = "invalidProjectNamesProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldThrowFactoryUrlExceptionIfProjectNameInvalid(String projectName) throws Exception {
        Map<String, String> projectAttributes = new HashMap<>();
        projectAttributes.put("pname", projectName);
        url.setProjectattributes(projectAttributes);
        validator.validateUrl(url);
    }

    @DataProvider(name = "validProjectNamesProvider")
    public Object[][] validProjectNames() {
        return new Object[][]{
                {"untitled"},
                {"Untitled"},
                {"untitled.project"},
                {"untitled-project"},
                {"untitled_project"},
                {"untitled01"},
                {"000011111"},
                {"0untitled"},
                {"UU"},
                {"untitled-proj12"},
                {"untitled.pro....111"},
                {"SampleStruts"}
        };
    }

    @DataProvider(name = "invalidProjectNamesProvider")
    public Object[][] invalidProjectNames() {
        return new Object[][]{
                {"-untitled"},
                {"untitled->3"},
                {"untitled__2%"},
                {"untitled_!@#$%^&*()_+?><"}
        };
    }

    @Test
    public void shouldBeAbleToValidateSimpleFactoryUrlObject() throws FactoryUrlException {
        validator.validateUrl(url);
    }

    @Test(dataProvider = "badSimpleFactoryUrlProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfObjectInvalid(SimpleFactoryUrl factoryUrl) throws FactoryUrlException {
        validator.validateUrl(factoryUrl);
    }

    @DataProvider(name = "badSimpleFactoryUrlProvider")
    public Object[][] invalidParameterssimpleFactoryUrlProvider() throws UnsupportedEncodingException {
        return new Object[][]{
                {new SimpleFactoryUrl("1.1", "notagit", VALID_REPOSITORY_URL, "commit123456789", null, null, false, null, null,
                                      "newBranch", null, null)},// invalid vcs
                {new SimpleFactoryUrl("1.1", "git", null, "commit123456789", null, null, false, null, null, "newBranch", null, null)},
                // invalid vcsurl
                {new SimpleFactoryUrl("1.1", "git", "", "commit123456789", null, null, false, null, null, "newBranch", null, null)},
                // invalid vcsurl
                {new SimpleFactoryUrl("1.0", "notagit", VALID_REPOSITORY_URL, "commit123456789", null, null, false, null, null,
                                      "newBranch", null, null)},
                // invalid v
        };
    }

    @Test
    public void shouldBeAbleToValidateAdvancedFactoryUrlObject() throws FactoryUrlException {
        validator.validateUrl(advUrl);
    }

    @Test(dataProvider = "badAdvancedFactoryUrlProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfObjectInvalid(AdvancedFactoryUrl factoryUrl) throws FactoryUrlException {
        validator.validateUrl(factoryUrl);
    }

    @DataProvider(name = "badAdvancedFactoryUrlProvider")
    public Object[][] invalidParametersAdvancedFactoryUrlProvider() throws UnsupportedEncodingException {
        return new Object[][]{
                {new AdvancedFactoryUrl("1.1", "notagit", VALID_REPOSITORY_URL, "commit123456789", null, null, false, null, null,
                                        "newBranch", null, null)},// invalid vcs
                {new AdvancedFactoryUrl("1.1", "git", null, "commit123456789", null, null, false, null, null, "newBranch", null, null)},
                // invalid vcsurl
                {new AdvancedFactoryUrl("1.1", "git", "", "commit123456789", null, null, false, null, null, "newBranch", null, null)},
                // invalid vcsurl
                {new AdvancedFactoryUrl("1.0", "notagit", VALID_REPOSITORY_URL, "commit123456789", null, null, false, null, null,
                                        "newBranch", null, null)},
                // invalid v
        };
    }
}
