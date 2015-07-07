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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
/*
import org.eclipse.che.api.factory.FactoryService;
import org.eclipse.che.api.factory.dto.Factory;
*/
import org.eclipse.che.api.workspace.server.dao.Member;
import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.dto.NewWorkspace;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.user.User;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Arrays;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Allows to create one factory workspace into other's account.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
public class FactoryWorkspaceInterceptor implements MethodInterceptor {

    @Inject
    @Named("api.endpoint")
    private String apiEndPoint;

    @Inject
    WorkspaceDao workspaceDao;

    @Inject
    MemberDao memberDao;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        /*
        NewWorkspace inbound = (NewWorkspace)invocation.getArguments()[0];
        if (!inbound.getAttributes().containsKey("sourceFactoryId")) {
            return invocation.proceed();
        }

        final SecurityContext oldContext = (SecurityContext)invocation.getArguments()[1];
        User currentUser = EnvironmentContext.getCurrent().getUser();
        String sourceFactoryId = inbound.getAttributes().get("sourceFactoryId");
        String getFactoryUrl = fromUri(apiEndPoint).path(FactoryService.class)
                                                   .path(FactoryService.class, "getFactory")
                                                   .build(sourceFactoryId)
                                                   .toString();
        Link link = DtoFactory.getInstance().createDto(Link.class).withMethod("GET").withHref(getFactoryUrl);
        final Factory factory = HttpJsonHelper.request(Factory.class, link, Pair.of("validate", true));
        final org.eclipse.che.api.factory.dto.Workspace factoryWorkspace = factory.getWorkspace();

        if (factoryWorkspace == null || factoryWorkspace.getType() == null || factoryWorkspace.getType().equals("named")) {

            String ownerAccountId =
                    (factoryWorkspace != null && "owner".equals(factoryWorkspace.getLocation())) ? factory.getCreator().getAccountId()
                                                                                                 : inbound.getAccountId();

            for (Workspace ws : workspaceDao.getByAccount(ownerAccountId)) {
                if (!ws.isTemporary() && ws.getAttributes().containsKey("sourceFactoryId") &&
                    ws.getAttributes().get("sourceFactoryId").equals(sourceFactoryId)) {
                    try {
                        Member member = memberDao.getWorkspaceMember(ws.getId(), currentUser.getId());
                        if (member.getRoles().contains("workspace/developer")) {
                            return Response.ok(DtoFactory.getInstance().createDto(WorkspaceDescriptor.class).withId(ws.getId())
                                                         .withAccountId(ws.getAccountId())
                                                         .withTemporary(ws.isTemporary())
                                                         .withName(ws.getName())
                                                         .withAttributes(ws.getAttributes())).build();
                        }
                    } catch (NotFoundException nfe) {
                        //ok
                    }
                }
            }
        }

        boolean needAddOwner = false;
        if (factoryWorkspace != null && factoryWorkspace.getLocation() != null && factoryWorkspace.getLocation().equals("owner")) {
            // no need to add role if creator and user are the same (will throw role already exists exc).
            needAddOwner = !factory.getCreator().getUserId().equals(currentUser.getId());
            invocation.getArguments()[1] = new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return oldContext.getUserPrincipal();
                }

                @Override
                public boolean isUserInRole(String role) {
                    return role.equals("system/admin") || oldContext.isUserInRole(role);
                }

                @Override
                public boolean isSecure() {
                    return oldContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return oldContext.getAuthenticationScheme();
                }
            };
        }
        */
        Object result = invocation.proceed();
        /*
        WorkspaceDescriptor descriptor = (WorkspaceDescriptor)((Response)result).getEntity();

        if (needAddOwner) {
            memberDao.create(new Member().withWorkspaceId(descriptor.getId())
                                         .withUserId(factory.getCreator().getUserId())
                                         .withRoles(Arrays.asList("workspace/admin")));
        }

        if (!descriptor.isTemporary()) {
            memberDao.create(new Member().withWorkspaceId(descriptor.getId())
                                         .withUserId(currentUser.getId())
                                         .withRoles(Arrays.asList("workspace/developer")));
        }
        */
        return result;
    }
}
