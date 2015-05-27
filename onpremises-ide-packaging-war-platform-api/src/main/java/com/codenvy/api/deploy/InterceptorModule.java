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
package com.codenvy.api.deploy;

import com.codenvy.workspace.interceptor.AddWorkspaceMemberInterceptor;
import com.codenvy.workspace.interceptor.CreateWorkspaceInterceptor;
import com.codenvy.workspace.interceptor.FactoryResourcesInterceptor;
import com.codenvy.workspace.interceptor.FactoryWorkspaceInterceptor;
import com.codenvy.workspace.interceptor.RemoveUserInterceptor;
import com.codenvy.workspace.interceptor.RemoveWorkspaceMemberInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import static org.eclipse.che.inject.Matchers.names;

/**
 * Package api interceptors in guice container.
 *
 * @author Sergii Kabashniuk
 * @author Igor Vinokur
 *
 * copied from hosted-infrastructure
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        AddWorkspaceMemberInterceptor addWorkspaceMemberInterceptor = new AddWorkspaceMemberInterceptor();
        RemoveWorkspaceMemberInterceptor removeWorkspaceMemberInterceptor = new RemoveWorkspaceMemberInterceptor();
        CreateWorkspaceInterceptor createWorkspaceInterceptor = new CreateWorkspaceInterceptor();
        FactoryWorkspaceInterceptor factoryWorkspaceInterceptor = new FactoryWorkspaceInterceptor();
        FactoryResourcesInterceptor factoryResourcesInterceptor = new FactoryResourcesInterceptor();
        RemoveUserInterceptor removeUserInterceptor = new RemoveUserInterceptor();
        requestInjection(addWorkspaceMemberInterceptor);
        requestInjection(factoryWorkspaceInterceptor);
        requestInjection(createWorkspaceInterceptor);
        requestInjection(factoryResourcesInterceptor);
        requestInjection(removeWorkspaceMemberInterceptor);
        requestInjection(removeUserInterceptor);

        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        names("addMember"),
                        addWorkspaceMemberInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        names("create"),
                        createWorkspaceInterceptor, factoryWorkspaceInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceDao.class),
                        names("createTemporary").or(names("create")),
                        factoryResourcesInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        names("removeMember"),
                        removeWorkspaceMemberInterceptor);
        bindInterceptor(Matchers.subclassesOf(UserService.class),
                        names("remove"),
                        removeUserInterceptor);
        bind(com.codenvy.workspace.listener.StopAppOnRemoveWsListener.class).asEagerSingleton();
    }
}
