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
import com.codenvy.api.factory.dto.*;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.dto.server.DtoFactory;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.client.UserManager;
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

    @Mock
    private UserManager userManager;

    @Mock
    private FactoryBuilder builder;

    @InjectMocks
    private FactoryUrlBaseValidator validator;

    private SimpleDateFormat datetimeFormatter;

    private Factory url;

    @BeforeMethod
    public void setUp() {
        Factory nonencoded = DtoFactory.getInstance().createDto(Factory.class);
        nonencoded.setV("1.2");
        nonencoded.setVcs("git");
        nonencoded.setVcsurl(VALID_REPOSITORY_URL);
        nonencoded.setVcsinfo(false);
        url = nonencoded;

        datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountDoesNotExist() throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        doThrow(new AccountExistenceException()).when(accountManager).getAccountById(ID);

        // when, then
        validator.validateObject(url, false);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfVcsurlContainIncorrectEncodedSymbol() throws FactoryUrlException {
        // given
        url.setVcsurl("http://codenvy.com/git/04%2");

        // when, then
        validator.validateObject(url, false);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectSsh() throws FactoryUrlException {
        // given
        url.setVcsurl("ssh://codenvy@review.gerrithub.io:29418/codenvy/exampleProject");

        // when, then
        validator.validateObject(url, false);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectHttps() throws FactoryUrlException {
        // given
        url.setVcsurl("https://github.com/codenvy/example.git");

        // when, then
        validator.validateObject(url, false);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountHasNoCertainProperty() throws OrganizationServiceException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(null);
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateObject(url, false);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountHasIllegalTariffPlan() throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2050-11-21 11:11:11").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Personal Premium");

        // when, then
        validator.validateObject(url, false);
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
        validator.validateObject(url, false);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfOrgIdExpires() throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2012-11-30 11:21:15").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateObject(url, false);
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdIsValid() throws OrganizationServiceException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateObject(url, false);
    }

    @Test(dataProvider = "validProjectNamesProvider")
    public void shouldBeAbleToValidateValidProjectName(String projectName) throws Exception {
        // given
        url.setProjectattributes(DtoFactory.getInstance().createDto(ProjectAttributes.class).withPname(projectName));

        // when, then
        validator.validateObject(url, false);
    }

    @Test(dataProvider = "invalidProjectNamesProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldThrowFactoryUrlExceptionIfProjectNameInvalid(String projectName) throws Exception {
        // given
        url.setProjectattributes(DtoFactory.getInstance().createDto(ProjectAttributes.class).withPname(projectName));

        // when, then
        validator.validateObject(url, false);
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
    public void shouldBeAbleToValidateFactoryUrlObject() throws FactoryUrlException {
        validator.validateObject(url, false);
    }

    @Test(dataProvider = "badAdvancedFactoryUrlProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfObjectInvalid(Factory factoryUrl) throws FactoryUrlException {
        validator.validateObject(factoryUrl, false);
    }

    @DataProvider(name = "badAdvancedFactoryUrlProvider")
    public Object[][] invalidParametersFactoryUrlProvider() throws UnsupportedEncodingException {
        Factory adv1 = DtoFactory.getInstance().createDto(Factory.class);
        adv1.setV("1.1");
        adv1.setVcs("notagit");
        adv1.setVcsurl(VALID_REPOSITORY_URL);
        adv1.setCommitid("commit123456789");
        adv1.setVcsinfo(false);
        adv1.setVcsbranch("newBranch");

        Factory adv2 = DtoFactory.getInstance().createDto(Factory.class);
        adv2.setV("1.1");
        adv2.setVcs("git");
        adv2.setVcsurl(null);
        adv2.setCommitid("commit123456789");
        adv2.setVcsinfo(false);
        adv2.setVcsbranch("newBranch");

        Factory adv3 = DtoFactory.getInstance().createDto(Factory.class);
        adv3.setV("1.1");
        adv3.setVcs("git");
        adv3.setVcsurl("");
        adv3.setCommitid("commit123456789");
        adv3.setVcsinfo(false);
        adv3.setVcsbranch("newBranch");

        return new Object[][]{
                {adv1},// invalid vcs
                {adv2},// invalid vcsurl
                {adv3}// invalid vcsurl
        };
    }

    @Test
    public void shouldBeAbleToValidateEncodedFactoryUrlObjectWithWelcomePageIfOrgIdIsValid()
            throws FactoryUrlException, OrganizationServiceException, ParseException {
        // given
        WelcomePage welcome = DtoFactory.getInstance().createDto(WelcomePage.class);
        WelcomeConfiguration conf1 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);
        WelcomeConfiguration conf2 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);

        conf1.setTitle("title");
        conf1.setIconurl("http://codenvy.com/favicon.ico");
        conf2.setTitle("title");
        conf2.setIconurl("http://codenvy.com/favicon.ico");

        welcome.setAuthenticated(conf1);
        welcome.setNonauthenticated(conf2);

        url.setWelcome(welcome);
        url.setOrgid(ID);
        when(accountManager.getAccountById(ID)).thenReturn(account);
        when(account.getAttribute("tariff_end_time")).thenReturn(Long.toString(datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(account.getAttribute("tariff_plan")).thenReturn("Managed Factory");

        // when, then
        validator.validateObject(url, true);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateEncodedFactoryUrlObjectWithWelcomePageIfOrgIdIsNull() throws FactoryUrlException {
        // given
        WelcomePage welcome = DtoFactory.getInstance().createDto(WelcomePage.class);
        WelcomeConfiguration conf1 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);
        WelcomeConfiguration conf2 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);

        conf1.setTitle("title");
        conf1.setIconurl("http://codenvy.com/favicon.ico");
        conf2.setTitle("title");
        conf2.setIconurl("http://codenvy.com/favicon.ico");

        welcome.setAuthenticated(conf1);
        welcome.setNonauthenticated(conf2);

        url.setWelcome(welcome);
        url.setOrgid(null);

        // when, then
        validator.validateObject(url, true);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateEncodedFactoryUrlObjectWithWelcomePageIfOrgIdIsEmpty() throws FactoryUrlException {
        // given
        WelcomePage welcome = DtoFactory.getInstance().createDto(WelcomePage.class);
        WelcomeConfiguration conf1 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);
        WelcomeConfiguration conf2 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);

        conf1.setTitle("title");
        conf1.setIconurl("http://codenvy.com/favicon.ico");
        conf2.setTitle("title");
        conf2.setIconurl("http://codenvy.com/favicon.ico");

        welcome.setAuthenticated(conf1);
        welcome.setNonauthenticated(conf2);

        url.setWelcome(welcome);
        url.setOrgid("");

        // when, then
        validator.validateObject(url, true);
    }
}
