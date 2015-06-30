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
package com.codenvy.api.metrics.server.limit.subscriber;

import com.codenvy.api.metrics.server.limit.ActiveTasksHolder;
import com.codenvy.api.metrics.server.limit.ResourcesWatchdog;
import com.codenvy.api.metrics.server.limit.WorkspaceResourcesUsageLimitChangedEvent;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergii Leschenko
 */
@Singleton
public class ChangeResourceUsageLimitSubscriber implements EventSubscriber<WorkspaceResourcesUsageLimitChangedEvent> {
    private final ActiveTasksHolder activeTasksHolder;
    private final EventService      eventService;

    @Inject
    public ChangeResourceUsageLimitSubscriber(ActiveTasksHolder activeTasksHolder,
                                              EventService eventService) {
        this.activeTasksHolder = activeTasksHolder;
        this.eventService = eventService;
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }

    @Override
    public void onEvent(WorkspaceResourcesUsageLimitChangedEvent event) {
        final ResourcesWatchdog watchdog = activeTasksHolder.getWatchdog(event.getWorkspaceId());
        if (watchdog != null) {
            watchdog.checkLimit();
        }
    }
}
