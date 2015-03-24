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
package com.codenvy.api.account.subscription.saas.limit;

import com.codenvy.api.account.AccountLocker;
import com.codenvy.api.account.metrics.MeteredBuildEventSubscriber;

import org.eclipse.che.api.builder.BuildQueue;
import org.eclipse.che.api.builder.internal.BuilderEvent;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.runner.internal.RunnerEvent;
import org.eclipse.che.api.workspace.server.dao.WorkspaceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import static org.eclipse.che.api.builder.internal.BuilderEvent.EventType.DONE;
import static org.eclipse.che.api.runner.internal.RunnerEvent.EventType.STOPPED;

/**
 * After run or build stops, checks that remaining RAM resources is enough, or block further runs for given account.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/15/15
 * @author Sergii Leschenko
 */
public class CheckRemainResourcesOnStopSubscriber {
    private static final Logger LOG = LoggerFactory.getLogger(CheckRemainResourcesOnStopSubscriber.class);

    private final EventService         eventService;
    private final WorkspaceDao         workspaceDao;
    private final AccountLocker        accountLocker;
    private final ResourcesChecker     resourcesChecker;
    final         BuildEventSubscriber buildEventSubscriber;
    final         RunEventSubscriber   runEventSubscriber;

    @Inject
    public CheckRemainResourcesOnStopSubscriber(EventService eventService,
                                                WorkspaceDao workspaceDao,
                                                AccountLocker accountLocker,
                                                ResourcesChecker resourcesChecker,
                                                BuildQueue buildQueue) {
        this.eventService = eventService;
        this.workspaceDao = workspaceDao;
        this.resourcesChecker = resourcesChecker;
        this.accountLocker = accountLocker;
        this.runEventSubscriber = new RunEventSubscriber();
        this.buildEventSubscriber = new BuildEventSubscriber(buildQueue);
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(runEventSubscriber);
        eventService.subscribe(buildEventSubscriber);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(runEventSubscriber);
        eventService.unsubscribe(buildEventSubscriber);
    }

    class RunEventSubscriber implements EventSubscriber<RunnerEvent> {
        @Override
        public void onEvent(RunnerEvent event) {
            if (STOPPED.equals(event.getType())) {
                checkRemainResources(event.getWorkspace());
            }
        }
    }

    class BuildEventSubscriber extends MeteredBuildEventSubscriber {
        public BuildEventSubscriber(BuildQueue buildQueue) {
            super(buildQueue);
        }

        @Override
        public void onMeteredBuildEvent(BuilderEvent event) {
            if (DONE.equals(event.getType())) {
                checkRemainResources(event.getWorkspace());
            }
        }
    }

    private void checkRemainResources(String workspaceId) {
        try {
            final String accountId = workspaceDao.getById(workspaceId).getAccountId();

            if (!resourcesChecker.hasAvailableResources(accountId)) {
                accountLocker.lockResources(accountId);
            }
        } catch (NotFoundException | ServerException e) {
            LOG.error("Error check remaining resources in workspace {} .", workspaceId);
        }
    }
}
