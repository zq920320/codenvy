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
import com.codenvy.api.factory.dto.AdvancedFactoryUrl;
import com.codenvy.api.factory.dto.SimpleFactoryUrl;
import com.codenvy.api.factory.dto.WelcomeConfiguration;
import com.codenvy.api.factory.dto.WelcomePage;
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

    @InjectMocks
    private FactoryUrlBaseValidator validator;

    private SimpleFactoryUrl url;

    private AdvancedFactoryUrl advUrl;

    private SimpleDateFormat datetimeFormatter;

    @BeforeMethod
    public void setUp() {
        SimpleFactoryUrl simpleFactoryUrl = DtoFactory.getInstance().createDto(SimpleFactoryUrl.class);
        simpleFactoryUrl.setV("1.0");
        simpleFactoryUrl.setVcs("git");
        simpleFactoryUrl.setVcsurl(VALID_REPOSITORY_URL);
        simpleFactoryUrl.setVcsinfo(false);
        url = simpleFactoryUrl;

        AdvancedFactoryUrl advancedFactoryUrl = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        advancedFactoryUrl.setV("1.1");
        advancedFactoryUrl.setVcs("git");
        advancedFactoryUrl.setVcsurl(VALID_REPOSITORY_URL);
        advancedFactoryUrl.setCommitid("123456798");
        advancedFactoryUrl.setVcsinfo(false);
        advUrl = advancedFactoryUrl;

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
    public void shouldNotValidateIfVcsurlContainIncorrectEncodedSymbol() throws FactoryUrlException {
        // given
        url.setVcsurl("http://codenvy.com/git/04%2");

        // when, then
        validator.validateUrl(url);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectSsh() throws FactoryUrlException {
        // given
        url.setVcsurl("ssh://codenvy@review.gerrithub.io:29418/codenvy/exampleProject");

        // when, then
        validator.validateUrl(url);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectHttps() throws FactoryUrlException {
        // given
        url.setVcsurl("https://github.com/codenvy/example.git");

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
        SimpleFactoryUrl f1 = DtoFactory.getInstance().createDto(SimpleFactoryUrl.class);
        f1.setV("1.1");
        f1.setVcs("notagit");
        f1.setVcsurl(VALID_REPOSITORY_URL);
        f1.setCommitid("commit123456789");
        f1.setVcsinfo(false);
        f1.setVcsbranch("newBranch");

        SimpleFactoryUrl f2 = DtoFactory.getInstance().createDto(SimpleFactoryUrl.class);
        f2.setV("1.1");
        f2.setVcs("git");
        f2.setCommitid("commit123456789");
        f2.setVcsinfo(false);
        f2.setVcsbranch("newBranch");

        SimpleFactoryUrl f3 = DtoFactory.getInstance().createDto(SimpleFactoryUrl.class);
        f3.setV("1.1");
        f3.setVcs("git");
        f3.setVcsurl("");
        f3.setCommitid("commit123456789");
        f3.setVcsinfo(false);
        f3.setVcsbranch("newBranch");

        SimpleFactoryUrl f4 = DtoFactory.getInstance().createDto(SimpleFactoryUrl.class);
        f4.setV("1.0");
        f4.setVcs("notagit");
        f4.setVcsurl(VALID_REPOSITORY_URL);
        f4.setCommitid("commit123456789");
        f4.setVcsinfo(false);
        f4.setVcsbranch("newBranch");

        return new Object[][]{
                {f1},// invalid vcs
                {f2},// invalid vcsurl
                {f3},// invalid vcsurl
                {f4},// invalid v
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
        AdvancedFactoryUrl adv1 = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        adv1.setV("1.1");
        adv1.setVcs("notagit");
        adv1.setVcsurl(VALID_REPOSITORY_URL);
        adv1.setCommitid("commit123456789");
        adv1.setVcsinfo(false);
        adv1.setVcsbranch("newBranch");

        AdvancedFactoryUrl adv2 = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        adv2.setV("1.1");
        adv2.setVcs("git");
        adv2.setVcsurl(null);
        adv2.setCommitid("commit123456789");
        adv2.setVcsinfo(false);
        adv2.setVcsbranch("newBranch");

        AdvancedFactoryUrl adv3 = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        adv3.setV("1.1");
        adv3.setVcs("git");
        adv3.setVcsurl("");
        adv3.setCommitid("commit123456789");
        adv3.setVcsinfo(false);
        adv3.setVcsbranch("newBranch");

        AdvancedFactoryUrl adv4 = DtoFactory.getInstance().createDto(AdvancedFactoryUrl.class);
        adv4.setV("1.0");
        adv4.setVcs("notagit");
        adv4.setVcsurl(VALID_REPOSITORY_URL);
        adv4.setCommitid("commit123456789");
        adv4.setVcsinfo(false);
        adv4.setVcsbranch("newBranch");

        return new Object[][]{
                {adv1},// invalid vcs
                {adv2},// invalid vcsurl
                {adv3},// invalid vcsurl
                {adv4},// invalid v
        };
    }

    @Test
    public void shouldBeAbleToValidateAdvancedFactoryUrlObjectWithWelcomePageIfOrgIdIsValid()
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

        advUrl.setWelcome(welcome);
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
        WelcomePage welcome = DtoFactory.getInstance().createDto(WelcomePage.class);
        WelcomeConfiguration conf1 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);
        WelcomeConfiguration conf2 = DtoFactory.getInstance().createDto(WelcomeConfiguration.class);

        conf1.setTitle("title");
        conf1.setIconurl("http://codenvy.com/favicon.ico");
        conf2.setTitle("title");
        conf2.setIconurl("http://codenvy.com/favicon.ico");

        welcome.setAuthenticated(conf1);
        welcome.setNonauthenticated(conf2);

        advUrl.setWelcome(welcome);
        advUrl.setOrgid(null);

        // when, then
        validator.validateUrl(advUrl);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateAdvancedFactoryUrlObjectWithWelcomePageIfOrgIdIsEmpty() throws FactoryUrlException {
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

        advUrl.setWelcome(welcome);
        advUrl.setOrgid("");

        // when, then
        validator.validateUrl(advUrl);
    }
}
