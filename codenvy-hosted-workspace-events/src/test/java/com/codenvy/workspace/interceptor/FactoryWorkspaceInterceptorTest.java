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
package com.codenvy.workspace.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
/*
import org.eclipse.che.api.factory.dto.Author;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.factory.dto.Workspace;
*/
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.NewWorkspace;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Max Shaposhnik
 *
 */

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryWorkspaceInterceptorTest {
    /*
    @Mock
    private WorkspaceDao workspaceDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private MethodInvocation invocation;

    @Mock
    private SecurityContext context;

    @Mock
    private Factory factory;

    @Mock
    private HttpJsonHelper.HttpJsonHelperImpl httpJsonHelper;

    @InjectMocks
    private FactoryWorkspaceInterceptor interceptor;

    private final  String SOURCE_FACTORY_ID = "factory123123";

    private final  String USER_ID = "user0098";

    @BeforeMethod
    public void setup() throws Throwable {
        EnvironmentContext context = EnvironmentContext.getCurrent();
        context.setUser(new UserImpl("test@user2.com", USER_ID, null, null, false));

        Field f = interceptor.getClass().getDeclaredField("apiEndPoint");
        f.setAccessible(true);
        f.set(interceptor, "http://dev.box.com/api");


        Field h = HttpJsonHelper.class.getDeclaredField("httpJsonHelperImpl");
        h.setAccessible(true);
        h.set(null, httpJsonHelper);

        Method method =
                WorkspaceService.class.getMethod("create", NewWorkspace.class, SecurityContext.class);

        when(invocation.getMethod()).thenReturn(method);
        when(invocation.proceed())
                .thenReturn(Response.ok(DtoFactory.getInstance().createDto(WorkspaceDescriptor.class).withTemporary(true)).build());

        when(httpJsonHelper.request(eq(Factory.class), anyString(), eq("GET"), isNull(), eq(Pair.of("validate", true))))
                .thenReturn(factory);
    }


    @Test
    public void shouldApplyDefaultFactoryLocationFlags() throws Throwable {
        NewWorkspace inbound = DtoFactory.getInstance().createDto(NewWorkspace.class);
        inbound.getAttributes().put("sourceFactoryId", SOURCE_FACTORY_ID);
        when(invocation.getArguments()).thenReturn(new Object[]{inbound, context});
        when(factory.getWorkspace()).thenReturn(DtoFactory.getInstance().createDto(Workspace.class).withLocation("owner"));
        when(factory.getCreator()).thenReturn(DtoFactory.getInstance().createDto(Author.class).withUserId(USER_ID));

        interceptor.invoke(invocation);

        Assert.assertTrue(((SecurityContext)invocation.getArguments()[1]).isUserInRole("system/admin"));
    }

    @Test
    public void shouldAddOwnerIfCreatedInAnotherAccount() throws Throwable {
        NewWorkspace inbound = DtoFactory.getInstance().createDto(NewWorkspace.class);
        inbound.getAttributes().put("sourceFactoryId", SOURCE_FACTORY_ID);
        when(invocation.getArguments()).thenReturn(new Object[]{inbound, context});
        when(factory.getWorkspace()).thenReturn(DtoFactory.getInstance().createDto(Workspace.class).withLocation("owner"));
        when(factory.getCreator()).thenReturn(DtoFactory.getInstance().createDto(Author.class).withUserId("somesome"));

        interceptor.invoke(invocation);

        verify(memberDao).create(any(Member.class));
    }


    @Test
    public void shouldReturnExistingWorkspaceIfTypeIsNamedAndWSFromFactoryExists() throws Throwable {
        NewWorkspace inbound = DtoFactory.getInstance().createDto(NewWorkspace.class);
        inbound.getAttributes().put("sourceFactoryId", SOURCE_FACTORY_ID);
        when(invocation.getArguments()).thenReturn(new Object[]{inbound, context});
        when(factory.getWorkspace()).thenReturn(DtoFactory.getInstance().createDto(Workspace.class).withLocation("owner").withType("named"));
        when(factory.getCreator()).thenReturn(DtoFactory.getInstance().createDto(Author.class).withUserId("somesome").withAccountId("acc"));
        when(memberDao.getWorkspaceMember(anyString(), anyString())).thenReturn(
                new Member().withRoles(Arrays.asList("workspace/developer")));

        org.eclipse.che.api.workspace.server.dao.Workspace ws = new org.eclipse.che.api.workspace.server.dao.Workspace().withId("testId");
        ws.getAttributes().put("sourceFactoryId", SOURCE_FACTORY_ID);
        when(workspaceDao.getByAccount(anyString())).thenReturn(Arrays.asList(ws));

        Object result = interceptor.invoke(invocation);

        WorkspaceDescriptor outbound = (WorkspaceDescriptor)((Response)result).getEntity();
        Assert.assertTrue(outbound.getId().equals("testId"));
    }
    */
}
