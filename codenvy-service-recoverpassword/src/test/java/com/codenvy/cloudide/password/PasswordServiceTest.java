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
package com.codenvy.cloudide.password;

import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.exception.UserException;
import com.codenvy.api.user.server.exception.UserNotFoundException;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.dto.server.DtoFactory;
import com.jayway.restassured.response.Response;

import org.codenvy.mail.MailSenderClient;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.tools.DependencySupplierImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/** Test of features from PassworService */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class PasswordServiceTest {
    @Mock
    private MailSenderClient mailService;

    @Mock
    private UserDao userDao;

    @Mock
    private RecoveryStorage recoveryStorage;

   /*
    * Not a mock objects
    */

    @InjectMocks
    private PasswordService passService;

    private static final String SERVICE_PATH = "/password";

    private static final String newPass = "new password";

    private static final String uuid = UUID.randomUUID().toString();

    private static final String username = "user@mail.com";

    private User user;

    private static Map<String, String> validationData = new HashMap<>();

    static {
        validationData.put("user.name", username);
    }

    @BeforeMethod
    public void setup() {
        user = DtoFactory.getInstance().createDto(User.class).withEmail(username);
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(UserDao.class, userDao);
    }

    @Test
    public void shouldBeAbleToSetupPass() throws Exception {
        when(recoveryStorage.isValid(uuid)).thenReturn(true);
        when(recoveryStorage.get(uuid)).thenReturn(validationData);
        when(userDao.getByAlias(username)).thenReturn(user);


        Response response =
                given().formParam("uuid", uuid).formParam("password", newPass).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 200);

        verify(recoveryStorage, times(1)).remove(uuid);
        verify(userDao, times(1)).update(any(User.class));
    }

    @Test
    public void shouldRespond403OnSetupPassForInvalidRecord() {
        when(recoveryStorage.isValid(uuid)).thenReturn(false);

        Response response =
                given().formParam("uuid", uuid).formParam("password", "newpass").when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 403);
        assertEquals(response.body().asString(), "Setup password token is incorrect or has expired");

        verify(recoveryStorage, times(1)).remove(uuid);
        verifyZeroInteractions(userDao);
    }

    @Test
    public void shouldRespond404OnSetupPassForNonRegisteredUser() throws Exception {
        when(recoveryStorage.isValid(uuid)).thenReturn(true);
        when(recoveryStorage.get(uuid)).thenReturn(validationData);
        doThrow(new UserNotFoundException(username)).when(userDao).getByAlias(username);

        Response response =
                given().formParam("uuid", uuid).formParam("password", newPass).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 404);
        assertEquals(response.body().asString(), "User " + username + " is not registered in the system");

        verify(recoveryStorage, times(1)).remove(uuid);
        verify(userDao, never()).update(eq(user));
    }

    @Test
    public void shouldRespond500IfOtherErrorOccursOnSetupPass() throws Exception {
        when(recoveryStorage.isValid(uuid)).thenReturn(true);
        when(recoveryStorage.get(uuid)).thenReturn(validationData);
        when(userDao.getByAlias(username)).thenReturn(user);
        doThrow(new UserException("test")).when(userDao).update(any(User.class));

        Response response =
                given().formParam("uuid", uuid).formParam("password", newPass).when().post(SERVICE_PATH + "/setup");

        assertEquals(response.statusCode(), 500);
        assertEquals(response.body().asString(), "Unable to setup password. Please contact with administrators.");

        verify(recoveryStorage, times(0)).remove(uuid);
        verify(userDao).update(any(User.class));
    }

    @Test
    public void shouldBeAbleToSetupConfirmation() throws Exception {
        when(recoveryStorage.isValid(uuid)).thenReturn(true);
        when(recoveryStorage.get(uuid)).thenReturn(validationData);

        Response response = given().pathParam("uuid", uuid).when().get(SERVICE_PATH + "/verify/{uuid}");

        assertEquals(response.statusCode(), 200);
        assertEquals(response.body().asString(), "user@mail.com");
    }

    @Test
    public void shouldRespond403OnSetupConfirmationInvalidUuid() throws Exception {
        when(recoveryStorage.isValid(uuid)).thenReturn(false);

        Response response = given().pathParam("uuid", uuid).when().get(SERVICE_PATH + "/verify/{uuid}");

        assertEquals(response.statusCode(), 403);
        assertEquals(response.body().asString(), "Setup password token is incorrect or has expired");

        verify(recoveryStorage, times(1)).remove(uuid);
    }

    @Test
    public void shouldSetResponseStatus404IfUserIsntRegistered() throws Exception {
        when(userDao.getByAlias(eq(username))).thenReturn(null);

        Response response = given().pathParam("username", username).when().post(SERVICE_PATH + "/recover/{username}");

        assertEquals(response.statusCode(), 404);
        assertEquals(response.body().asString(), "User " + username + " is not registered in the system.");

        verifyZeroInteractions(mailService);
        verifyZeroInteractions(recoveryStorage);
    }

    @Test
    public void shouldBeAbleToUpdatePass() throws Exception {
        when(userDao.getByAlias(ADMIN_USER_NAME)).thenReturn(user);
        Response response =
                given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).formParam("password", "NEW " + ADMIN_USER_PASSWORD)
                        .when().post(SECURE_PATH + SERVICE_PATH + "/change");

        assertEquals(response.statusCode(), 200);

        verify(userDao).update(any(User.class));
    }

    @Test
    public void shouldRespond500OnChangingPassError() throws Exception {
        when(userDao.getByAlias(ADMIN_USER_NAME)).thenReturn(user);
        doThrow(new UserException("test")).when(userDao).update(any(User.class));

        Response response =
                given().auth().basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD).formParam("password", "NEW " + ADMIN_USER_PASSWORD)
                        .when().post(SECURE_PATH + SERVICE_PATH + "/change");

        assertEquals(response.statusCode(), 500);
        assertEquals(response.body().asString(), "Unable to change password. Please contact with administrators.");
    }
}
