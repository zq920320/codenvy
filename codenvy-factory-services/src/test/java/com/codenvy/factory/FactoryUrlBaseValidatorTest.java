/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.account.shared.dto.Member;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.core.ServerException;
import com.codenvy.api.factory.FactoryBuilder;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.factory.dto.ProjectAttributes;
import com.codenvy.api.factory.dto.Restriction;
import com.codenvy.api.factory.dto.WelcomePage;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryUrlBaseValidatorTest {

    private static String VALID_REPOSITORY_URL = "http://github.com/codenvy/cloudide";

    private static final String ID = "id";

    @Mock
    private AccountDao accountDao;


    @Mock
    private UserDao userDao;

    @Mock
    UserProfileDao profileDao;

    @Mock
    private FactoryBuilder builder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private FactoryUrlBaseValidator validator;

    private SimpleDateFormat datetimeFormatter;

    private Member member;

    private Factory url;

    @BeforeMethod
    public void setUp() throws ParseException,   NotFoundException, ServerException {
        Factory nonencoded = DtoFactory.getInstance().createDto(Factory.class);
        nonencoded.setV("1.2");
        nonencoded.setVcs("git");
        nonencoded.setVcsurl(VALID_REPOSITORY_URL);
        url = nonencoded;

        datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        User user = DtoFactory.getInstance().createDto(User.class).withId("userid");

        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TrackedFactory")
                                              .withStartDate(datetimeFormatter.parse("2000-11-21 11:11:11").getTime())
                                              .withEndDate(datetimeFormatter.parse("2022-11-30 11:21:15").getTime());
        member = DtoFactory.getInstance().createDto(Member.class).withUserId("userid").withRoles(Arrays.asList("account/owner"));
        when(accountDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        when(accountDao.getMembers(anyString())).thenReturn(Arrays.asList(member));
        when(userDao.getById("userid")).thenReturn(user);
        when(profileDao.getById(anyString())).thenReturn(DtoFactory.getInstance().createDto(Profile.class));
        url.setOrgid(ID);
        url.setUserid("userid");
    }

    @Test
    public void shouldBeAbleToValidateFactoryUrlObject() throws FactoryUrlException {
        validator.validateVcs(url);
        validator.validateProjectName(url);
        validator.validateOrgid(url);
        validator.validateTrackedFactoryAndParams(url);
    }


    @Test(expectedExceptions = FactoryUrlException.class,
          expectedExceptionsMessageRegExp =
                  "The parameter vcsurl has a value submitted http://codenvy.com/git/04%2 with a value that is unexpected. " +
                  "For more information, please visit: http://docs.codenvy.com/user/creating-factories/factory-parameter-reference/.")
    public void shouldNotValidateIfVcsurlContainIncorrectEncodedSymbol() throws FactoryUrlException {
        // given
        url.setVcsurl("http://codenvy.com/git/04%2");

        // when, then
        validator.validateVcs(url);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectSsh() throws FactoryUrlException {
        // given
        url.setVcsurl("ssh://codenvy@review.gerrithub.io:29418/codenvy/exampleProject");

        // when, then
        validator.validateVcs(url);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectHttps() throws FactoryUrlException {
        // given
        url.setVcsurl("https://github.com/codenvy/example.git");

        // when, then
        validator.validateVcs(url);
    }

    @Test(dataProvider = "badAdvancedFactoryUrlProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfVcsOrVcsUrlIsInvalid(Factory factoryUrl) throws FactoryUrlException {
        validator.validateVcs(factoryUrl);
    }

    @DataProvider(name = "badAdvancedFactoryUrlProvider")
    public Object[][] invalidParametersFactoryUrlProvider() throws UnsupportedEncodingException {
        Factory adv1 = DtoFactory.getInstance().createDto(Factory.class);
        adv1.setV("1.1");
        adv1.setVcs("notagit");
        adv1.setVcsurl(VALID_REPOSITORY_URL);

        Factory adv2 = DtoFactory.getInstance().createDto(Factory.class);
        adv2.setV("1.1");
        adv2.setVcs("git");
        adv2.setVcsurl(null);

        Factory adv3 = DtoFactory.getInstance().createDto(Factory.class);
        adv3.setV("1.1");
        adv3.setVcs("git");
        adv3.setVcsurl("");

        return new Object[][]{
                {adv1},// invalid vcs
                {adv2},// invalid vcsurl
                {adv3}// invalid vcsurl
        };
    }

    @Test(dataProvider = "invalidProjectNamesProvider", expectedExceptions = FactoryUrlException.class,
          expectedExceptionsMessageRegExp = "Project name must contain only Latin letters, digits or these following special characters -._.")
    public void shouldThrowFactoryUrlExceptionIfProjectNameInvalid(String projectName) throws Exception {
        // given
        url.setProjectattributes(DtoFactory.getInstance().createDto(ProjectAttributes.class).withPname(projectName));

        // when, then
        validator.validateProjectName(url);
    }

    @Test(dataProvider = "validProjectNamesProvider")
    public void shouldBeAbleToValidateValidProjectName(String projectName) throws Exception {
        // given
        url.setProjectattributes(DtoFactory.getInstance().createDto(ProjectAttributes.class).withPname(projectName));

        // when, then
        validator.validateProjectName(url);
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
    public void shouldBeAbleToValidateIfOrgIdIsValid() throws  FactoryUrlException, ParseException {
        validator.validateOrgid(url);
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdAndOwnerAreValid()
            throws  FactoryUrlException, ParseException {
        // when, then
        validator.validateOrgid(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountDoesNotExist() throws  FactoryUrlException, NotFoundException, ServerException {
        when(accountDao.getMembers(anyString())).thenReturn(Collections.<Member>emptyList());

        validator.validateOrgid(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class, expectedExceptionsMessageRegExp = "You are not authorized to use this orgid.")
    public void shouldNotValidateIfFactoryOwnerIsNotOrgidOwner()
            throws  FactoryUrlException, ParseException, 
                   ServerException {
        Member wronMember  = member;
        wronMember.setUserId("anotheruserid");
        when(accountDao.getMembers(anyString())).thenReturn(Arrays.asList(wronMember));

        // when, then
        validator.validateOrgid(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfSubscriptionHasIllegalTariffPlan()
            throws FactoryUrlException, ParseException, ServerException, NotFoundException {
        // given
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("INVALID")
                                              .withStartDate(datetimeFormatter.parse("2000-11-21 11:11:11").getTime())
                                              .withEndDate(datetimeFormatter.parse("2050-11-21 11:11:11").getTime());
        when(accountDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        // when, then
        validator.validateTrackedFactoryAndParams(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfOrgIdIsExpired() throws FactoryUrlException, ParseException, ServerException, NotFoundException {
        // given
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TrackedFactory")
                                              .withStartDate(datetimeFormatter.parse("2000-11-21 11:11:11").getTime())
                                              .withEndDate(datetimeFormatter.parse("2000-11-21 11:11:11").getTime());
        when(accountDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        // when, then
        validator.validateTrackedFactoryAndParams(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfOrgIdIsNotValidYet() throws FactoryUrlException, ParseException, ServerException, NotFoundException {
        // given
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TrackedFactory")
                                              .withStartDate(datetimeFormatter.parse("2049-11-21 11:11:11").getTime())
                                              .withEndDate(datetimeFormatter.parse("2050-11-21 11:11:11").getTime());
        when(accountDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        // when, then
        validator.validateTrackedFactoryAndParams(url);
    }

    @Test
    public void shouldValidateIfHostNameIsLegal() throws  FactoryUrlException, ParseException {
        // given
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("notcodenvy.com"));

        when(request.getHeader("Referer")).thenReturn("http://notcodenvy.com/factories-examples");

        // when, then
        validator.validateTrackedFactoryAndParams(url);
    }

    @Test
    public void shouldValidateIfRefererIsRelativeAndCurrentHostnameIsEqualToRequiredHostName()
            throws  FactoryUrlException, ParseException {
        // given
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("next.codenvy.com"));

        when(request.getHeader("Referer")).thenReturn("/factories-examples");
        when(request.getServerName()).thenReturn("next.codenvy.com");

        // when, then
        validator.validateTrackedFactoryAndParams(url);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateEncodedFactoryWithWelcomePageIfOrgIdIsEmpty() throws FactoryUrlException {
        // given
        WelcomePage welcome = DtoFactory.getInstance().createDto(WelcomePage.class);

        url.setWelcome(welcome);
        url.setOrgid("");

        // when, then
        validator.validateTrackedFactoryAndParams(url);
    }

    @Test(dataProvider = "trackedFactoryParametersProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfThereIsTrackedOnlyParameterAndOrgidIsNull(Factory factory)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, FactoryUrlException {
        factory.setOrgid(null);
        validator.validateTrackedFactoryAndParams(factory);
    }

    @DataProvider(name = "trackedFactoryParametersProvider")
    public Object[][] trackedFactoryParametersProvider() throws URISyntaxException, IOException, NoSuchMethodException {
        return new Object[][]{
                {url.withWelcome(DtoFactory.getInstance().createDto(WelcomePage.class))},
                {url.withRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValidsince(123456))},
                {url.withRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValiduntil(123456798))},
                {url.withRestriction(DtoFactory.getInstance().createDto(Restriction.class).withPassword("123456"))},
                {url.withRestriction(DtoFactory.getInstance().createDto(Restriction.class).withMaxsessioncount(1234))},
                {url.withRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("host"))},
                {url.withRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRestrictbypassword(true))}
        };
    }
}
