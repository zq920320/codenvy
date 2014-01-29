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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private SimpleDateFormat datetimeFormatter;

    @BeforeMethod
    public void setUp() {
        url = new SimpleFactoryUrl("1.0", "git", VALID_REPOSITORY_URL, null, null, null, false, null, null, null, null, null);

        advUrl = new AdvancedFactoryUrl("1.1", "git", VALID_REPOSITORY_URL, "123456798", null, null, false, null, null, null, null, null);

        datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
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
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateUrl(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountHasIllegalTariffPlan() throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2050-11-21 11:11:11").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Personal Premium");

        // when, then
        validator.validateUrl(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfTariffEndTimePropertyHasIllegalFormat()
            throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn("smth");
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateUrl(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfOrgIdExpires() throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2012-11-30 11:21:15").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateUrl(url);
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdIsValid() throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateUrl(url);
    }

    @Test(dataProvider = "validProjectNamesProvider")
    public void shouldBeAbleToValidateValidProjectName(String projectName) throws Exception {
        // given
        Map<String, String> projectAttributes = new HashMap<>();
        projectAttributes.put("pname", projectName);
        url.setProjectattributes(projectAttributes);

        // when, then
        validator.validateUrl(url);
    }

    @Test(dataProvider = "invalidProjectNamesProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldThrowFactoryUrlExceptionIfProjectNameInvalid(String projectName) throws Exception {
        // given
        Map<String, String> projectAttributes = new HashMap<>();
        projectAttributes.put("pname", projectName);
        url.setProjectattributes(projectAttributes);

        // when, then
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

    @Test
    public void shouldBeAbleToValidateAdvancedFactoryUrlObjectWithWelcomePageIfOrgIdIsValid()
            throws FactoryUrlException, OrganizationServiceException, ParseException {
        // given
        advUrl.setWelcome(new WelcomePage(new WelcomeConfiguration("title", null, "http://codenvy.com/favicon.ico"),
                                          new WelcomeConfiguration("title", null, "http://codenvy.com/favicon.ico")));
        advUrl.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateUrl(advUrl);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateAdvancedFactoryUrlObjectWithWelcomePageIfOrgIdIsNull() throws FactoryUrlException {
        // given
        advUrl.setWelcome(new WelcomePage(new WelcomeConfiguration("title", null, "http://codenvy.com/favicon.ico"),
                                          new WelcomeConfiguration("title", null, "http://codenvy.com/favicon.ico")));
        advUrl.setOrgid(null);

        // when, then
        validator.validateUrl(advUrl);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateAdvancedFactoryUrlObjectWithWelcomePageIfOrgIdIsEmpty() throws FactoryUrlException {
        // given
        advUrl.setWelcome(new WelcomePage(new WelcomeConfiguration("title", null, "http://codenvy.com/favicon.ico"),
                                          new WelcomeConfiguration("title", null, "http://codenvy.com/favicon.ico")));
        advUrl.setOrgid("");

        // when, then
        validator.validateUrl(advUrl);
    }
}
