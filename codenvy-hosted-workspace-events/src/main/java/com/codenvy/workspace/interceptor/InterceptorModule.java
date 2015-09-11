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

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;

import static org.eclipse.che.inject.Matchers.names;

/**
 * Package api interceptors in guice container.
 *
 * @author Sergii Kabashniuk
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        AddWorkspaceMemberInterceptor addWorkspaceMemberInterceptor = new AddWorkspaceMemberInterceptor();
        RemoveWorkspaceMemberInterceptor removeWorkspaceMemberInterceptor = new RemoveWorkspaceMemberInterceptor();
        CreateWorkspaceInterceptor createWorkspaceInterceptor = new CreateWorkspaceInterceptor();
        FactoryWorkspaceInterceptor factoryWorkspaceInterceptor = new FactoryWorkspaceInterceptor();
        FactoryResourcesInterceptor factoryResourcesInterceptor = new FactoryResourcesInterceptor();
        requestInjection(addWorkspaceMemberInterceptor);
        requestInjection(factoryWorkspaceInterceptor);
        requestInjection(createWorkspaceInterceptor);
        requestInjection(factoryResourcesInterceptor);
        requestInjection(removeWorkspaceMemberInterceptor);

        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        names("addMember"),
                        addWorkspaceMemberInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        names("create")),
                        factoryWorkspaceInterceptor, createWorkspaceInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceManager.class),
                        names("createWorkspace"),
                        factoryResourcesInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        names("removeMember"),
                        removeWorkspaceMemberInterceptor);

        bind(com.codenvy.workspace.listener.StopAppOnRemoveWsListener.class).asEagerSingleton();
    }
}
