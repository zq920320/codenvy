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

import com.codenvy.api.factory.FactoryBuilder;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.factory.dto.*;
import com.codenvy.api.organization.server.dao.OrganizationDao;
import com.codenvy.api.organization.server.exception.OrganizationException;
import com.codenvy.api.organization.server.exception.OrganizationNotFoundException;
import com.codenvy.api.organization.shared.dto.Organization;
import com.codenvy.api.organization.shared.dto.Subscription;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;
import com.codenvy.api.user.server.exception.UserException;
import com.codenvy.api.user.server.exception.UserProfileException;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryUrlBaseValidatorTest {
    private static String TF_PARAMETER_WITHOUT_ORGID_MESSAGE =
            "You have provided a Tracked Factory parameter %s, and you do not have a valid orgId %s. You could have " +
            "provided the wrong code, your subscription has expired, or you do not have a valid subscription account." +
            " Please contact info@codenvy.com with any questions.";

    private static String VALID_REPOSITORY_URL = "http://github.com/codenvy/cloudide";

    private static final String ID = "id";

    @Mock
    private OrganizationDao organizationDao;


    @Mock
    private UserDao userDao;

    @Mock
    UserProfileDao profileDao;

    @Mock
    private Organization organization;

    @Mock
    private FactoryBuilder builder;

    @Mock
    private HttpServletRequest request;

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
        url = nonencoded;

        datetimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        datetimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Test
    public void shouldBeAbleToValidateFactoryUrlObject() throws FactoryUrlException {
        validator.validate(url, false, request);
    }


    @Test(expectedExceptions = FactoryUrlException.class,
          expectedExceptionsMessageRegExp =
                  "The parameter vcsurl has a value submitted http://codenvy.com/git/04%2 with a value that is unexpected. " +
                  "For more information, please visit: http://docs.codenvy.com/user/creating-factories/factory-parameter-reference/.")
    public void shouldNotValidateIfVcsurlContainIncorrectEncodedSymbol() throws FactoryUrlException {
        // given
        url.setVcsurl("http://codenvy.com/git/04%2");

        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectSsh() throws FactoryUrlException {
        // given
        url.setVcsurl("ssh://codenvy@review.gerrithub.io:29418/codenvy/exampleProject");

        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldValidateIfVcsurlIsCorrectHttps() throws FactoryUrlException {
        // given
        url.setVcsurl("https://github.com/codenvy/example.git");

        // when, then
        validator.validate(url, false, request);
    }

    @Test(dataProvider = "badAdvancedFactoryUrlProvider", expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfVcsOrVcsUrlIsInvalid(Factory factoryUrl) throws FactoryUrlException {
        validator.validate(factoryUrl, false, request);
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
        validator.validate(url, false, request);
    }

    @Test(dataProvider = "validProjectNamesProvider")
    public void shouldBeAbleToValidateValidProjectName(String projectName) throws Exception {
        // given
        url.setProjectattributes(DtoFactory.getInstance().createDto(ProjectAttributes.class).withPname(projectName));

        // when, then
        validator.validate(url, false, request);
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
    public void shouldBeAbleToValidateIfOrgIdIsValid() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldBeAbleToValidateIfOrgIdAndOwnerAreValid()
            throws OrganizationException, FactoryUrlException, ParseException, UserException, UserProfileException {
        // given
        url.setOrgid(ID);
        url.setUserid("userid");
        User user = DtoFactory.getInstance().createDto(User.class).withId("userid");
        when(userDao.getById("userid")).thenReturn(user);
        when(organizationDao.getById(ID)).thenReturn(organization);
        when(profileDao.getById(anyString())).thenReturn(DtoFactory.getInstance().createDto(Profile.class));
        when(organization.getOwner()).thenReturn("userid");
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2020-11-21 11:11:11").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountDoesNotExist() throws OrganizationException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        doThrow(OrganizationNotFoundException.doesNotExistWithId(ID)).when(organizationDao).getById(ID);

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class, expectedExceptionsMessageRegExp = "You are not authorized to use this orgid.")
    public void shouldNotValidateIfFactoryOwnerIsNotOrgidOwner()
            throws OrganizationException, FactoryUrlException, ParseException, UserException, UserProfileException {
        // given
        url.setOrgid(ID);
        url.setUserid("userid");
        User user = DtoFactory.getInstance().createDto(User.class).withId("userid");
        when(userDao.getById("userid")).thenReturn(user);
        when(organizationDao.getById(ID)).thenReturn(organization);
        when(profileDao.getById(anyString())).thenReturn(DtoFactory.getInstance().createDto(Profile.class));
        when(organization.getOwner()).thenReturn("anotheruserid");

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountHasNoCertainProperty() throws OrganizationException, FactoryUrlException {
        // given
        url.setOrgid(ID);
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(null);
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfAccountHasIllegalTariffPlan() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("INVALID")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2050-11-21 11:11:11").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfTariffEndTimePropertyHasIllegalFormat()
            throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate("smth");
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfOrgIdIsExpired() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2000-11-21 11:11:11").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldValidateIfCurrentDateIsAfterValidsince() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValidsince(new Date().getTime()));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfCurrentDateIsBeforeValidsince()
            throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 4, 1);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValidsince(calendar.getTimeInMillis()));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldValidateIfCurrentDateIsBeforeValidUntil() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 4, 1);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValiduntil(calendar.getTimeInMillis()));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateIfCurrentDateIsAfterValidUntil() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withValiduntil(new Date().getTime() - 1));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));

        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldValidateIfHostNameIsLegal() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("notcodenvy.com"));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        when(request.getHeader("Referer")).thenReturn("http://notcodenvy.com/factories-examples");

        // when, then
        validator.validate(url, false, request);
    }

    @Test
    public void shouldValidateIfRefererIsRelativeAndCurrentHostnameIsEqualToRequiredHostName()
            throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("next.codenvy.com"));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        when(request.getHeader("Referer")).thenReturn("/factories-examples");
        when(request.getServerName()).thenReturn("next.codenvy.com");

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class,
          expectedExceptionsMessageRegExp = "This Factory has its access restricted by certain hostname. Your client does not match the specified policy. Please contact the owner of this Factory for more information.")
    public void shouldNotValidateIfRefererIsEmpty() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("notcodenvy.com"));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        when(request.getHeader("Referer")).thenReturn(null);

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class,
          expectedExceptionsMessageRegExp = "This Factory has its access restricted by certain hostname. Your client does not match the specified policy. Please contact the owner of this Factory for more information.")
    public void shouldNotValidateIfRefererIsNotEqualToHostName() throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("notcodenvy.com"));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        when(request.getHeader("Referer")).thenReturn("http://codenvy.com/factories-examples");

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class,
          expectedExceptionsMessageRegExp = "This Factory has its access restricted by certain hostname. Your client does not match the specified policy. Please contact the owner of this Factory for more information.")
    public void shouldNotValidateIfRefererIsRelativeUrlAndCurrentHostnameIsNotEqualToRequired()
            throws OrganizationException, FactoryUrlException, ParseException {
        // given
        url.setOrgid(ID);
        url.setRestriction(DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("notcodenvy.com"));
        Subscription subscription = DtoFactory.getInstance().createDto(Subscription.class)
                                              .withServiceId("TF")
                                              .withEndDate(Long.toString(
                                                      datetimeFormatter.parse("2022-11-30 11:21:15").getTime()));
        when(organizationDao.getSubscriptions(ID)).thenReturn(Arrays.asList(subscription));
        when(request.getHeader("Referer")).thenReturn("/factories-examples");

        // when, then
        validator.validate(url, false, request);
    }

    @Test(expectedExceptions = FactoryUrlException.class)
    public void shouldNotValidateEncodedFactoryWithWelcomePageIfOrgIdIsEmpty() throws FactoryUrlException {
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
        validator.validate(url, true, request);
    }

    @Test(dataProvider = "trackedFactoryParametersProvider")
    public <T> void shouldNotValidateIfThereIsTrackedOnlyParameterAndOrgidIsNull(String methodName, T arg, Class<T> argClass,
                                                                                 String parameter)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // given
        Factory.class.getMethod(methodName, argClass).invoke(url, arg);

        // when
        try {
            validator.validate(url, false, request);
        } catch (FactoryUrlException e) {
            // then
            if (!String.format(TF_PARAMETER_WITHOUT_ORGID_MESSAGE, parameter, null).equals(e.getLocalizedMessage())) {
                fail();
            }
        }
    }

    @DataProvider(name = "trackedFactoryParametersProvider")
    public static Object[][] trackedFactoryParametersProvider() throws URISyntaxException, IOException, NoSuchMethodException {
        return new Object[][]{
                {"setWelcome", DtoFactory.getInstance().createDto(WelcomePage.class), WelcomePage.class, "welcome"},
                {"setRestriction", DtoFactory.getInstance().createDto(Restriction.class).withValidsince(123456), Restriction.class,
                 "validsince"},
                {"setRestriction", DtoFactory.getInstance().createDto(Restriction.class).withValiduntil(123456798), Restriction.class,
                 "validuntil"},
                {"setRestriction", DtoFactory.getInstance().createDto(Restriction.class).withPassword("123456"), Restriction.class,
                 "password"},
                {"setRestriction", DtoFactory.getInstance().createDto(Restriction.class).withMaxsessioncount(1234), Restriction.class,
                 "maxsessioncount"},
                {"setRestriction", DtoFactory.getInstance().createDto(Restriction.class).withRefererhostname("host"), Restriction.class,
                 "refererhostname"},
                {"setRestriction", DtoFactory.getInstance().createDto(Restriction.class).withRestrictbypassword(true), Restriction.class,
                 "restrictbypassword"}
        };
    }
}
