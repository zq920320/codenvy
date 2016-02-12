/*
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

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

@Listeners(value = {MockitoTestNGListener.class})
public class AddWorkspaceMemberInterceptorTest {
//    @Mock
//    private MailSenderClient mailSenderClient;
//
//    @Mock
//    private UserDao userDao;
//
//    @Mock
//    private MemberDao memberDao;
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
//    private AddWorkspaceMemberInterceptor interceptor;
//
//    @BeforeMethod
//    public void setup() throws Exception {
//        setInterceptorPrivateFieldValue("sendEmailOnMemberAdded", true);
//
//        EnvironmentContext context = EnvironmentContext.getCurrent();
//        context.setUser(new UserImpl("test@user2.com", "askd123123", null, null, false));
//        context.setAccountId("AccountID");
//
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
//    public void shouldNotSendEmailIfJoinToTemporaryWs() throws Throwable {
//        Method method = WorkspaceService.class.getMethod("addMember", String.class, NewMembership.class, SecurityContext.class);
//
//        when(invocation.proceed()).thenReturn(status(CREATED).entity(memberDescriptor).build());
//        when(invocation.getMethod()).thenReturn(method);
//        when(memberDescriptor.getWorkspaceReference())
//                .thenReturn(DtoFactory.getInstance().createDto(WorkspaceReference.class).withName("testWSName").withTemporary(true));
//
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldNotSendEmailIfNotificationIsTurnedOff() throws Throwable {
//        setInterceptorPrivateFieldValue("sendEmailOnMemberAdded", false);
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldSendEmail() throws Throwable {
//        setInterceptorPrivateFieldValue("apiEndpoint", "http://dev.box.com/api");
//
//        String recipient = "test@user.com";
//        Method method = WorkspaceService.class.getMethod("addMember", String.class, NewMembership.class, SecurityContext.class);
//
//        when(invocation.proceed()).thenReturn(status(CREATED).entity(memberDescriptor).build());
//        when(invocation.getMethod()).thenReturn(method);
//        when(memberDescriptor.getUserId()).thenReturn("user1234566");
//        when(memberDescriptor.getWorkspaceReference())
//                .thenReturn(DtoFactory.getInstance().createDto(WorkspaceReference.class).withId("ws29301").withName("testWSName"));
//        when(userDao.getById(anyString())).thenReturn(user);
//        when(user.getEmail()).thenReturn(recipient);
//
//        when(memberDao.getWorkspaceMembers(eq("ws29301"))).thenReturn(Arrays.asList(new Member(), new Member()));
//        when(accountDao.getMembers(eq("AccountID"))).thenReturn(Arrays.asList(new org.eclipse.che.api.account.server.dao.Member()));
//
//        interceptor.invoke(invocation);
//        verify(mailSenderClient)
//                .sendMail(anyString(), eq(recipient), anyString(), anyString(), eq("text/html; charset=utf-8"),
//                          anyString(), anyMapOf(String.class, String.class));
//    }
//
//    @Test
//    public void shouldNotSendEmailOnFirstUser() throws Throwable {
//        setInterceptorPrivateFieldValue("apiEndpoint", "http://dev.box.com/api");
//
//        String recipient = "test@user.com";
//        Method method = WorkspaceService.class.getMethod("addMember", String.class, NewMembership.class, SecurityContext.class);
//
//        when(invocation.proceed()).thenReturn(status(CREATED).entity(memberDescriptor).build());
//        when(invocation.getMethod()).thenReturn(method);
//        when(memberDescriptor.getUserId()).thenReturn("user1234566");
//        when(memberDescriptor.getWorkspaceReference())
//                .thenReturn(DtoFactory.getInstance().createDto(WorkspaceReference.class).withId("ws29301").withName("testWSName"));
//        when(userDao.getById(anyString())).thenReturn(user);
//        when(user.getEmail()).thenReturn(recipient);
//
//        when(memberDao.getWorkspaceMembers(eq("ws29301"))).thenReturn(Arrays.asList(new Member()));
//
//
//        interceptor.invoke(invocation);
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    private void setInterceptorPrivateFieldValue(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
//        Field notificationTurnedOn = interceptor.getClass().getDeclaredField(fieldName);
//        notificationTurnedOn.setAccessible(true);
//        notificationTurnedOn.set(interceptor, value);
//    }
}
