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

import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import javax.ws.rs.core.Response;

/**
 * Package api interceptors in guice container.
 *
 * @author Sergii Kabashniuk
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        AddWorkspaceMemberInterceptor addWorkspaceMemberInterceptor = new AddWorkspaceMemberInterceptor();
        CreateWorkspaceInterceptor createWorkspaceInterceptor = new CreateWorkspaceInterceptor();
        FactoryWorkspaceInterceptor factoryWorkspaceInterceptor = new FactoryWorkspaceInterceptor();
        FactoryResourcesInterceptor factoryResourcesInterceptor = new FactoryResourcesInterceptor();
        requestInjection(addWorkspaceMemberInterceptor);
        requestInjection(factoryWorkspaceInterceptor);
        requestInjection(createWorkspaceInterceptor);
        requestInjection(factoryResourcesInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        Matchers.returns(Matchers.subclassesOf(Response.class)),
                        addWorkspaceMemberInterceptor, createWorkspaceInterceptor, factoryWorkspaceInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceDao.class),
                        Matchers.any(),
                        factoryResourcesInterceptor);
        bind(com.codenvy.workspace.listener.StopAppOnRemoveWsListener.class).asEagerSingleton();
    }
}
