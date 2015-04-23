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
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.factory.FactoryService;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.factory.dto.WorkspaceResources;
import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Allows to set internal resource attributes into workspace.
 * @author Max Shaposhnik
 *
 */
public class FactoryResourcesInterceptor implements MethodInterceptor {

    @Inject
    @Named("api.endpoint")
    private  String   apiEndPoint;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if ("create".equals(invocation.getMethod().getName()) || "createTemporary".equals(invocation.getMethod().getName())) {
            Workspace inbound = (Workspace)invocation.getArguments()[0];
            if (inbound.getAttributes().containsKey("sourceFactoryId")) {
                String getFactoryUrl =
                        fromUri(apiEndPoint).path(FactoryService.class).path(FactoryService.class, "getFactory")
                                            .build(inbound.getAttributes().get("sourceFactoryId")).toString();
                Link link = DtoFactory.getInstance().createDto(Link.class).withMethod("GET").withHref(getFactoryUrl);
                Factory factory = HttpJsonHelper.request(Factory.class, link, Pair.of("validate", true));
                org.eclipse.che.api.factory.dto.Workspace factoryWorkspace = factory.getWorkspace();
                if (factoryWorkspace ==  null || factoryWorkspace.getResources() == null) {
                    return invocation.proceed();
                }
                WorkspaceResources resources = factoryWorkspace.getResources();
                if (resources.getRunnerRam() != null) {
                    inbound.getAttributes()
                           .put(Constants.RUNNER_MAX_MEMORY_SIZE, Integer.toString(resources.getRunnerRam()));
                }
                if (resources.getRunnerTimeout() != null) {
                    inbound.getAttributes()
                           .put(Constants.RUNNER_LIFETIME, Integer.toString(resources.getRunnerTimeout()));
                }
                if (resources.getBuilderTimeout() != null) {
                    inbound.getAttributes().put(org.eclipse.che.api.builder.internal.Constants.BUILDER_EXECUTION_TIME,
                                                Integer.toString(resources.getBuilderTimeout()));
                }
            }
        }
        return invocation.proceed();
    }
}
