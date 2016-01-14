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

//TODO fix it after account refactoring

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

@Listeners(value = {MockitoTestNGListener.class})
public class CreateWorkspaceInterceptorTest {
//    @Mock
//    private MailSenderClient mailSenderClient;
//
//    @Mock
//    private UserDao userDao;
//
//    @Mock
//    private AccountDao accountDao;
//
//    @Mock
//    private User user;
//
//    @Mock
//    private Profile profile;
//
//    @Mock
//    private MethodInvocation invocation;
//
//    @Mock
//    private UsersWorkspaceDto workspaceDescriptor;
//
//    @Mock
//    private WorkspaceDao workspaceDao;
//
//    @Mock
//    private WsActivityEventSender wsActivityEventSender;
//
//    @InjectMocks
//    private CreateWorkspaceInterceptor interceptor;
//
//    private String recipient = "test@user.com";
//
//    @BeforeMethod
//    public void setup() throws Exception {
//        setInterceptorPrivateFieldValue("sendEmailOnWorkspaceCreated", true);
//
//        EnvironmentContext context = EnvironmentContext.getCurrent();
//        context.setUser(new UserImpl(recipient, "askd123123", null, null, false));
//        when(workspaceDescriptor.getAccountId()).thenReturn("acc123");
//        when(workspaceDao.getByAccount(anyString())).thenReturn(Collections.emptyList());
//        doNothing().when(wsActivityEventSender).onActivity(anyString(), anyBoolean());
//    }
//
//    @Test(expectedExceptions = ConflictException.class)
//    public void shouldNotSendEmailIfInvocationThrowsException() throws Throwable {
//        when(invocation.proceed()).thenThrow(new ConflictException("conflict"));
//
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldNotSendEmailIfInvocationToAnotherMethod() throws Throwable {
//        when(invocation.proceed()).thenReturn(Response.ok(workspaceDescriptor).build());
//        when(invocation.getMethod()).thenReturn(WorkspaceService.class.getMethod("remove", String.class));
//
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldNotSendEmailIfNotificationIsTurnedOff() throws Throwable {
//        when(invocation.proceed()).thenReturn(Response.ok(workspaceDescriptor).build());
//        setInterceptorPrivateFieldValue("sendEmailOnWorkspaceCreated", false);
//
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldNotSendEmailIfWorkspaceiIsTemporary() throws Throwable {
//        when(invocation.proceed()).thenReturn(Response.ok(workspaceDescriptor).build());
//        when(workspaceDescriptor.isTemporary()).thenReturn(true);
//
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//
//    @Test
//    public void shouldNotSendEmailIfWorkspaceiIsNotFirstInAccount() throws Throwable {
//        when(invocation.proceed()).thenReturn(Response.ok(workspaceDescriptor).build());
//        List<Workspace> otherWs = Arrays.asList(new Workspace().withId("anotherid"));
//        when(workspaceDao.getByAccount(anyString())).thenReturn(otherWs);
//
//        interceptor.invoke(invocation);
//
//        verifyZeroInteractions(mailSenderClient);
//    }
//
//    @Test
//    public void shouldSendEmail() throws Throwable {
//
//        setInterceptorPrivateFieldValue("apiEndpoint", "http://dev.box.com/api");
//        setInterceptorPrivateFieldValue("freeGbh", "10");
//        setInterceptorPrivateFieldValue("freeLimit", "4096");
//
//        User accountOwner = mock(User.class);
//        Member member = mock(Member.class);
//        Method method = WorkspaceService.class.getMethod("create", NewWorkspace.class, SecurityContext.class);
//        when(invocation.proceed()).thenReturn(Response.ok(workspaceDescriptor).build());
//        when(invocation.getMethod()).thenReturn(method);
//        when(userDao.getById(anyString())).thenReturn(user);
//        when(workspaceDescriptor.getAccountId()).thenReturn("AccountId");
//        when(member.getRoles()).thenReturn(singletonList("account/owner"));
//        when(member.getUserId()).thenReturn("userId");
//        when(accountDao.getMembers(eq("AccountId"))).thenReturn(singletonList(member));
//        when(userDao.getById("userId")).thenReturn(accountOwner);
//        when(user.getEmail()).thenReturn(recipient);
//
//        interceptor.invoke(invocation);
//
//        verify(accountOwner).getEmail();
//        verify(mailSenderClient)
//                .sendMail(anyString(), eq(recipient), anyString(), anyString(), eq("text/html"),
//                          anyString(), anyMapOf(String.class, String.class));
//    }
//
//    private void setInterceptorPrivateFieldValue(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
//        Field notificationTurnedOn = interceptor.getClass().getDeclaredField(fieldName);
//        notificationTurnedOn.setAccessible(true);
//        notificationTurnedOn.set(interceptor, value);
//    }
}
