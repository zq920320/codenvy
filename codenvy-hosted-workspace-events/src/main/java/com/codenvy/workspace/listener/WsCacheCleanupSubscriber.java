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
package com.codenvy.workspace.listener;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.service.http.WorkspaceInfoCache;
import com.codenvy.workspace.event.DeleteWorkspaceEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Remove workspace from {@link WorkspaceInfoCache} if {@link DeleteWorkspaceEvent}
 * come to from {@link EventService}.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WsCacheCleanupSubscriber {
    private final EventService                          eventService;
    private final EventSubscriber<DeleteWorkspaceEvent> subscriber;

    @Inject
    public WsCacheCleanupSubscriber(EventService eventService, final WorkspaceInfoCache cache) {
        this.eventService = eventService;
        this.subscriber = new EventSubscriber<DeleteWorkspaceEvent>() {
            @Override
            public void onEvent(DeleteWorkspaceEvent event) {
                cache.removeById(event.getWorkspace().getId());
                cache.removeByName(event.getWorkspace().getName());
            }
        };
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(subscriber);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(subscriber);
    }
}
