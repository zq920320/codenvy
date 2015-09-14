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
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
  /*
import org.eclipse.che.api.factory.FactoryService;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.factory.dto.WorkspaceResources;
*/
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Allows to set internal resource attributes into workspace.
 *
 * @author Max Shaposhnik
 */
public class FactoryResourcesInterceptor implements MethodInterceptor {
    @Inject
    @Named("api.endpoint")
    private String apiEndPoint;

    @Inject
    @Named("factory.runner.lifetime")
    private String runnerLifetime;

    @Inject
    @Named("factory.runner.ram")
    private String runnerRam;

    @Inject
    @Named("factory.builder.execution_time")
    private String builderExecutionTime;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        WorkspaceConfig inbound = (WorkspaceConfig)invocation.getArguments()[0];

        //Workspace created without factory
        if (!inbound.getAttributes().containsKey("sourceFactoryId")) {
            return invocation.proceed();
        }
        /*
        String getFactoryUrl = fromUri(apiEndPoint).path(FactoryService.class)
                                                   .path(FactoryService.class, "getFactory")
                                                   .build(inbound.getAttributes().get("sourceFactoryId"))
                                                   .toString();
        Link link = DtoFactory.getInstance().createDto(Link.class).withMethod("GET").withHref(getFactoryUrl);
        Factory factory = HttpJsonHelper.request(Factory.class, link, Pair.of("validate", true));
        org.eclipse.che.api.factory.dto.Workspace factoryWorkspace = factory.getWorkspace();
        if (factoryWorkspace == null || factoryWorkspace.getResources() == null) {
            inbound.getAttributes().put(Constants.RUNNER_MAX_MEMORY_SIZE, runnerRam);
            inbound.getAttributes().put(Constants.RUNNER_LIFETIME, runnerLifetime);
            inbound.getAttributes().put(org.eclipse.che.api.builder.internal.Constants.BUILDER_EXECUTION_TIME, builderExecutionTime);
            return invocation.proceed();
        }
        //looking one-by-one because they may be set partially
        WorkspaceResources resources = factoryWorkspace.getResources();
        inbound.getAttributes().put(Constants.RUNNER_MAX_MEMORY_SIZE,
                                    resources.getRunnerRam() != null ? Integer.toString(resources.getRunnerRam()) : runnerRam);
        inbound.getAttributes().put(Constants.RUNNER_LIFETIME,
                                    resources.getRunnerTimeout() != null ? Integer.toString(resources.getRunnerTimeout())
                                                                         : runnerLifetime);

        inbound.getAttributes().put(org.eclipse.che.api.builder.internal.Constants.BUILDER_EXECUTION_TIME,
                                    resources.getBuilderTimeout() != null ? Integer.toString(resources.getBuilderTimeout())
                                                                          : builderExecutionTime);
        */
        return invocation.proceed();
    }
}
