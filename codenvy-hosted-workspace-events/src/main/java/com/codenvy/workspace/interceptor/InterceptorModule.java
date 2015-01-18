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

import com.codenvy.api.workspace.server.WorkspaceService;
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
        requestInjection(addWorkspaceMemberInterceptor);
        requestInjection(createWorkspaceInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        Matchers.returns(Matchers.subclassesOf(Response.class)),
                        addWorkspaceMemberInterceptor);
        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        Matchers.returns(Matchers.subclassesOf(Response.class)),
                        createWorkspaceInterceptor);
        bind(com.codenvy.workspace.listener.StopAppOnRemoveWsListener.class).asEagerSingleton();
    }
}
