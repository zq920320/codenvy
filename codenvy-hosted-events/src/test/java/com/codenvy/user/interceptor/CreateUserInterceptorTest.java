/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.user.interceptor;


import com.codenvy.BaseInterceptorTest;

import org.aopalliance.intercept.MethodInvocation;
import org.codenvy.mail.MailSenderClient;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.user.server.dao.User;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class CreateUserInterceptorTest extends BaseInterceptorTest {
    @Mock
    private MailSenderClient      mailSenderClient;
    @Mock
    private User                  user;
    @Mock
    private MethodInvocation      invocation;
    @InjectMocks
    private CreateUserInterceptor interceptor;

    private String recipient = "test@user.com";

    @BeforeMethod
    public void setup() throws Exception {
        setInterceptorPrivateFieldValue(interceptor, "apiEndpoint", "http://localhost/api");
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotSendEmailIfInvocationThrowsException() throws Throwable {
        when(invocation.proceed()).thenThrow(new ConflictException("conflict"));

        interceptor.invoke(invocation);

        verifyZeroInteractions(mailSenderClient);
    }

    @Test
    public void shouldSendEmail() throws Throwable {
        when(invocation.getArguments()).thenReturn(new Object[] {user});
        when(user.getEmail()).thenReturn(recipient);

        interceptor.invoke(invocation);

        verify(mailSenderClient).sendMail(anyString(),
                                          eq(recipient),
                                          isNull(String.class),
                                          anyString(),
                                          eq("text/html"),
                                          anyString(),
                                          anyMapOf(String.class, String.class));
    }
}
