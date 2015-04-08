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
package com.codenvy.workspace.listener;

import com.codenvy.workspace.event.CreateWorkspaceEvent;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.dao.Workspace;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Set attributes to temporary workspaces that make runner/builder use custom resources for factory
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class FactoryWorkspaceResourceProvider implements EventSubscriber<CreateWorkspaceEvent> {
    private static final Logger LOG                    = LoggerFactory.getLogger(FactoryWorkspaceResourceProvider.class);
    private static final String RUNNER_LIFETIME        = "factory.runner.lifetime";
    private static final String RUNNER_RAM             = "factory.runner.ram";
    private static final String BUILDER_EXECUTION_TIME = "factory.builder.execution_time";

    private final String runnerLifetime;
    private final String runnerRam;
    private final String builderExecutionTime;

    private final WorkspaceDao workspaceDao;
    private final EventService eventService;

    @Inject
    public FactoryWorkspaceResourceProvider(@Nullable @Named(RUNNER_LIFETIME) String runnerLifetime,
                                            @Nullable @Named(RUNNER_RAM) String runnerRam,
                                            @Nullable @Named(BUILDER_EXECUTION_TIME) String builderExecutionTime,
                                            WorkspaceDao workspaceDao,
                                            EventService eventService) {
        this.runnerLifetime = runnerLifetime;
        this.runnerRam = runnerRam;
        this.builderExecutionTime = builderExecutionTime;
        this.workspaceDao = workspaceDao;
        this.eventService = eventService;
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(CreateWorkspaceEvent event) {
        Workspace createdWorkspace = event.getWorkspace();
        if (createdWorkspace.isTemporary()) {
            try {
                final Workspace workspace = workspaceDao.getById(createdWorkspace.getId());
                final Map<String, String> attributes = workspace.getAttributes();

                // common factory workspace
                setIfValuePresents(attributes, "codenvy:runner_lifetime", runnerLifetime);
                setIfValuePresents(attributes, "codenvy:runner_ram", runnerRam);
                setIfValuePresents(attributes, "codenvy:builder_execution_time", builderExecutionTime);

                workspaceDao.update(workspace.withAttributes(attributes));
            } catch (ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private void setIfValuePresents(Map<String, String> attributes, String key, String value) {
        if (value != null && !value.isEmpty()) {
            attributes.put(key, value);
        }
    }
}
