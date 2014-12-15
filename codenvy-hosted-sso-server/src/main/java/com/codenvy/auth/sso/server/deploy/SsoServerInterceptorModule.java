/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.auth.sso.server.deploy;

import com.codenvy.api.workspace.server.WorkspaceService;
import com.codenvy.auth.sso.server.interceptor.AddWorkspaceMemberInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import javax.ws.rs.core.Response;

/**
 * @author Sergii Kabashniuk
 */
public class SsoServerInterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        AddWorkspaceMemberInterceptor addWorkspaceMemberInterceptor = new AddWorkspaceMemberInterceptor();

        requestInjection(addWorkspaceMemberInterceptor);

        bindInterceptor(Matchers.subclassesOf(WorkspaceService.class),
                        Matchers.returns(Matchers.subclassesOf(Response.class)),
                        addWorkspaceMemberInterceptor);

    }
}
