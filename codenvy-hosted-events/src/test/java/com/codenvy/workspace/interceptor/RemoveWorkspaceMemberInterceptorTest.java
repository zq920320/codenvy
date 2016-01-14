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
package com.codenvy.workspace.interceptor;

//TODO fix it after memberships refactoring

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

@Listeners(value = {MockitoTestNGListener.class})
public class RemoveWorkspaceMemberInterceptorTest {
//    @Mock
//    private MailSenderClient mailSenderClient;
//
//    @Mock
//    private UserDao userDao;
//
//    @Mock
//    private WorkspaceDao workspaceDao;
//
//    @Mock
//    private AccountDao accountDao;
//
//    @Mock
//    private EnvironmentContext environmentContext;
//
//    @Mock
//    private User user;
//
//    @Mock
//    private MethodInvocation invocation;
//
//    @Mock
//    private MemberDescriptor memberDescriptor;
//
//    @InjectMocks
//    private RemoveWorkspaceMemberInterceptor interceptor;
//
//    @BeforeMethod
//    public void setup() throws Exception {
//        setInterceptorPrivateFieldValue("sendEmailOnMemberRemoved", true);
//
//        EnvironmentContext context = EnvironmentContext.getCurrent();
//        context.setUser(new UserImpl("test@user2.com", "askd123123", null, null, false));
//        context.setAccountId("AccountID");
//    }
//
//    @Test
//    public void shouldNotSendEmailIfNotificationIsTurnedOff() throws Throwable {
//        setInterceptorPrivateFieldValue("sendEmailOnMemberRemoved", false);
//        interceptor.invoke(invocation);
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test(expectedExceptions = ConflictException.class)
//    public void shouldNotSendEmailIfInvocationThrowsException() throws Throwable {
//        when(invocation.proceed()).thenThrow(new ConflictException("conflict"));
//        interceptor.invoke(invocation);
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldSendEmail() throws Throwable {
//        setInterceptorPrivateFieldValue("apiEndpoint", "http://dev.box.com/api");
//
//        String recipient = "test@user.com";
//        Method method = WorkspaceService.class.getMethod("removeMember", String.class, String.class, SecurityContext.class);
//
//        when(invocation.getMethod()).thenReturn(method);
//        when(invocation.getArguments()).thenReturn(new Object[]{"ws123", "user123"});
//        when(userDao.getById(anyString())).thenReturn(user);
//        when(user.getEmail()).thenReturn(recipient);
//        when(workspaceDao.getById(anyString())).thenReturn(new Workspace().withName("someName"));
//
//        when(accountDao.getMembers(eq("AccountID"))).thenReturn(Arrays.asList(new Member()));
//
//        interceptor.invoke(invocation);
//        verify(mailSenderClient)
//                .sendMail(anyString(), eq(recipient), anyString(), anyString(), eq("text/html; charset=utf-8"),
//                          anyString(), anyMapOf(String.class, String.class));
//    }
//
//    private void setInterceptorPrivateFieldValue(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
//        Field notificationTurnedOn = interceptor.getClass().getDeclaredField(fieldName);
//        notificationTurnedOn.setAccessible(true);
//        notificationTurnedOn.set(interceptor, value);
//    }
}
