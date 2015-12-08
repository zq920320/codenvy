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

import com.codenvy.api.account.AddDefaultAccountIdInterceptor;
import com.google.inject.AbstractModule;

import org.eclipse.che.api.workspace.server.WorkspaceService;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

// TODO bind commented interceptors after memberships refactoring

/**
 * Package api interceptors in guice container.
 *
 * @author Sergii Kabashniuk
 * @author Yevhenii Voevodin
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
//        final FactoryWorkspaceInterceptor factoryWorkspaceInterceptor = new FactoryWorkspaceInterceptor();
//        requestInjection(factoryWorkspaceInterceptor);
//
//        final CreateWorkspaceInterceptor createWorkspaceInterceptor = new CreateWorkspaceInterceptor();
//        requestInjection(createWorkspaceInterceptor);
//        bindInterceptor(subclassesOf(WorkspaceService.class),
//                        names("create"),
//                        factoryWorkspaceInterceptor,
//                        createWorkspaceInterceptor);
//
//        final FactoryResourcesInterceptor factoryResourcesInterceptor = new FactoryResourcesInterceptor();
//        requestInjection(factoryResourcesInterceptor);
//        bindInterceptor(subclassesOf(WorkspaceManager.class), names("createWorkspace"), factoryResourcesInterceptor);
//
//        final AddWorkspaceMemberInterceptor addWorkspaceMemberInterceptor = new AddWorkspaceMemberInterceptor();
//        requestInjection(addWorkspaceMemberInterceptor);
//        bindInterceptor(subclassesOf(WorkspaceService.class), names("addMember"), addWorkspaceMemberInterceptor);
//
//        final RemoveWorkspaceMemberInterceptor removeWorkspaceMemberInterceptor = new RemoveWorkspaceMemberInterceptor();
//        requestInjection(removeWorkspaceMemberInterceptor);
//        bindInterceptor(subclassesOf(WorkspaceService.class), names("removeMember"), removeWorkspaceMemberInterceptor);

        final AddDefaultAccountIdInterceptor addDefaultAccountIdInterceptor = new AddDefaultAccountIdInterceptor();
        requestInjection(addDefaultAccountIdInterceptor);
        bindInterceptor(subclassesOf(WorkspaceService.class), names("create"), addDefaultAccountIdInterceptor);
        bindInterceptor(subclassesOf(WorkspaceService.class), names("startById"), addDefaultAccountIdInterceptor);
        bindInterceptor(subclassesOf(WorkspaceService.class), names("startByName"), addDefaultAccountIdInterceptor);
        bindInterceptor(subclassesOf(WorkspaceService.class), names("startTemporary"), addDefaultAccountIdInterceptor);

        bind(com.codenvy.workspace.listener.StopAppOnRemoveWsListener.class).asEagerSingleton();
    }
}
