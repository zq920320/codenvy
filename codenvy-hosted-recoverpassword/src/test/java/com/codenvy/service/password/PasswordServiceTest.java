
/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.service.password;

import com.jayway.restassured.response.Response;

import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.commons.lang.IoUtil.getResource;
import static org.eclipse.che.commons.lang.IoUtil.readAndCloseQuietly;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/** Test of features from PasswordService */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class PasswordServiceTest {
    private static final String              SERVICE_PATH           = "/password";
    private static final String              NEW_PASSWORD           = "new password";
    private static final String              UUID                   = java.util.UUID.randomUUID().toString();
    private static final String              USERNAME               = "user@mail.com";
    private static final String              CODENVY_MASTERHOST_URL = "http://localhost:1111";

    @Mock
    private UriInfo uriInfo;

    @Mock
    private MailSenderClient mailService;

    @Mock
    private UserDao userDao;

    @Mock
    private UserProfileDao userProfileDao;

    @Mock
    private RecoveryStorage recoveryStorage;

    @Mock
    private Profile profile;

    @Spy
    private User user;

    private PasswordService passService;

    @SuppressWarnings("unused")
     ApiExceptionMapper exceptionMapper;

    @BeforeMethod
    public void setup() throws Exception {
        passService = new PasswordService(mailService,
                                          userDao,
                                          recoveryStorage,
                                          userProfileDao,
                                          "Codenvy <noreply@codenvy.com>",
                                          "Codenvy Password Recovery",
                                          1);

        doReturn(USERNAME).when(user).getEmail();
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(UserDao.class, userDao);
        dependencies.addComponent(UserProfileDao.class, userProfileDao);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl().uri("http://localhost:1111"));
        final Field uriField = passService.getClass()
                                          .getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(passService, uriInfo);
    }

    @Test
    public void shouldBeAbleToSetupPass() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USERNAME);
        when(userDao.getByAlias(USERNAME)).thenReturn(user);
        doReturn("userId").when(user).getId();
        when(userProfileDao.getById(eq("userId"))).thenReturn(profile);
        when(profile.getAttributes()).thenReturn(Collections.<String, String>emptyMap());

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 204);

        verify(recoveryStorage).remove(UUID);
        verify(userDao).update(any(User.class));
        verify(userProfileDao, never()).update(profile);
    }

    @Test
    public void shouldBeAbleToSetupPassAndRemoveResetPassFlagFromProfile() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USERNAME);
        when(userDao.getByAlias(USERNAME)).thenReturn(user);
        doReturn("userId").when(user).getId();
        when(userProfileDao.getById(eq("userId"))).thenReturn(profile);
        when(profile.getAttributes()).thenReturn(new HashMap<String, String>() {{
            put("resetPassword", "true");
        }});

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 204);

        verify(recoveryStorage).remove(UUID);
        verify(userDao).update(any(User.class));
        verify(userProfileDao).update(profile);
    }

    @Test
    public void shouldRespond403OnSetupPassForInvalidRecord() {
        when(recoveryStorage.isValid(UUID)).thenReturn(false);

        Response response =
                given().formParam("uuid", UUID).formParam("password", "newpass").when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 403);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Setup password token is incorrect or has expired");

        verify(recoveryStorage, times(1)).remove(UUID);
        verifyZeroInteractions(userDao);
    }

    @Test
    public void shouldRespond404OnSetupPassForNonRegisteredUser() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USERNAME);
        doThrow(new NotFoundException(USERNAME)).when(userDao).getByAlias(USERNAME);

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 404);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "User " + USERNAME + " is not registered in the system.");
        verify(recoveryStorage, times(1)).remove(UUID);
        verify(userDao, never()).update(eq(user));
    }

    @Test
    public void shouldRespond500IfOtherErrorOccursOnSetupPass() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);
        when(recoveryStorage.get(UUID)).thenReturn(USERNAME);
        when(userDao.getByAlias(USERNAME)).thenReturn(user);
        doThrow(new ServerException("test")).when(userDao).update(any(User.class));

        Response response =
                given().formParam("uuid", UUID).formParam("password", NEW_PASSWORD).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 500);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "Unable to setup password. Please contact support.");

        verify(recoveryStorage, times(1)).remove(UUID);
        verify(userDao).update(any(User.class));
    }

    @Test
    public void shouldBeAbleToSetupConfirmation() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(true);

        Response response = given().pathParam("uuid", UUID).when().get(SERVICE_PATH + "/verify/{uuid}");

        assertEquals(response.statusCode(), 204);
        verify(recoveryStorage).isValid(eq(UUID));
    }

    @Test
    public void shouldRespond403OnSetupConfirmationInvalidUuid() throws Exception {
        when(recoveryStorage.isValid(UUID)).thenReturn(false);

        Response response = given().pathParam("uuid", UUID).when().get(SERVICE_PATH + "/verify/{uuid}");

        assertEquals(response.statusCode(), 403);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "Setup password token is incorrect or has expired");
        verify(recoveryStorage).remove(UUID);
    }

    @Test
    public void shouldBeAbleToRecoverPassword() throws Exception {
        when(userDao.getByAlias(USERNAME)).thenReturn(user);
        when(recoveryStorage.generateRecoverToken(eq(USERNAME))).thenReturn(UUID);
        Response response = given().pathParam("username", USERNAME).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 204);
        Map<String, String> templateProperties = new HashMap<>();
        templateProperties.put("com.codenvy.masterhost.url", CODENVY_MASTERHOST_URL);
        templateProperties.put("id", UUID);
        templateProperties.put("validation.token.age.message", "1 hour");
        verify(mailService).sendMail(eq("Codenvy <noreply@codenvy.com>"),
                                     eq(USERNAME),
                                     (String)isNull(),
                                     eq("Codenvy Password Recovery"),
                                     eq(MediaType.TEXT_HTML),
                                     eq(readAndCloseQuietly(getResource("/email-templates/password_recovery.html"))),
                                     any(Map.class));
    }

    @Test
    public void shouldSetResponseStatus404IfUserIsntRegistered() throws Exception {
        when(userDao.getByAlias(eq(USERNAME))).thenThrow(NotFoundException.class);

        Response response = given().pathParam("username", USERNAME).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 404);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(), "User " + USERNAME + " is not registered in the system.");
        verifyZeroInteractions(mailService);
        verifyZeroInteractions(recoveryStorage);
    }

    @Test
    public void shouldRespond500IfProblemOnEmailSendingOccurs() throws Exception {
        doThrow(new MessagingException()).when(mailService).sendMail(anyString(),
                                                                     anyString(),
                                                                     anyString(),
                                                                     anyString(),
                                                                     anyString(),
                                                                     anyString(),
                                                                     (Map)anyObject());

        Response response = given().pathParam("username", USERNAME).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 500);
        assertEquals(unwrapDto(response, ServiceError.class).getMessage(),
                     "Unable to recover password. Please contact support or try later.");
    }

    private static <T> T unwrapDto(com.jayway.restassured.response.Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }
}
