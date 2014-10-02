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
package com.codenvy.workspace.interceptor;

import com.codenvy.api.core.ConflictException;
import com.codenvy.api.user.server.dao.Profile;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.User;
import com.codenvy.api.workspace.server.WorkspaceService;
import com.codenvy.api.workspace.shared.dto.NewWorkspace;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;
import com.codenvy.commons.env.EnvironmentContext;
import com.codenvy.commons.user.UserImpl;

import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class CreateWorkspaceInterceptorTest {

    @Mock
    private MailSenderClient mailSenderClient;

    @Mock
    private UserDao userDao;

    @Mock
    private User user;

    @Mock
    private Profile profile;

    @Mock
    private MethodInvocation invocation;

    @Mock
    private WorkspaceDescriptor workspaceDescriptor;

    @InjectMocks
    private CreateWorkspaceInterceptor interceptor;

    private String recipient = "test@user.com";

    @BeforeMethod
    public void setup() throws Exception {
        EnvironmentContext context = EnvironmentContext.getCurrent();
        context.setUser(new UserImpl(recipient, "askd123123", null, null, false));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotSendEmailIfInvocationThrowsException() throws Throwable {
        when(invocation.proceed()).thenThrow(new ConflictException("conflict"));
        interceptor.invoke(invocation);
        verifyZeroInteractions(mailSenderClient);
    }

    @Test
    public void shouldNotSendEmailIfInvocationToAnotherMethod() throws Throwable {
        when(invocation.getMethod()).thenReturn(WorkspaceService.class.getMethod("remove", String.class));
        interceptor.invoke(invocation);
        verifyZeroInteractions(mailSenderClient);
    }

    @Test
    public void shouldSendEmail() throws Throwable {
        Method method =
                WorkspaceService.class.getMethod("create", NewWorkspace.class, SecurityContext.class);
        Field f = interceptor.getClass().getDeclaredField("apiEndpoint");
        f.setAccessible(true);
        f.set(interceptor, "http://dev.box.com/api");

        when(invocation.proceed()).thenReturn(Response.ok(workspaceDescriptor).build());
        when(invocation.getMethod()).thenReturn(method);
        when(userDao.getById(anyString())).thenReturn(user);
        when(user.getEmail()).thenReturn(recipient);
        interceptor.invoke(invocation);
        verify(mailSenderClient)
                .sendMail(anyString(), eq(recipient), anyString(), anyString(), eq("text/html"),
                          anyString(), anyMapOf(String.class, String.class));

    }

}
