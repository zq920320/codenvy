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
package com.codenvy.workspace.activity;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.api.core.notification.EventSubscriber;
import com.codenvy.workspace.event.WsActivityEvent;
import com.codenvy.workspace.listener.WorkspaceRemovalListener;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Workspace activity listener.
 *
 * @author Alexander Garagatyi
 * @author Max Shaposhnik
 */
@Singleton
public class WsActivityListener {
    public static final String TEMPORARY_WS_STOP_TIME  = "workspace.inactive.temporary_stop_time";
    public static final String PERSISTENT_WS_STOP_TIME = "workspace.inactive.persistent_stop_time";

    private final Cache<String, Boolean> persistentWSCache;
    private final Cache<String, Boolean> temporaryWSCache;

    private final EventService eventService;

    @Inject
    public WsActivityListener(WorkspaceRemovalListener removalListener, @Named(TEMPORARY_WS_STOP_TIME) long temporaryTime,
                              @Named(PERSISTENT_WS_STOP_TIME) long persistentTime, EventService eventService) {
        this.eventService = eventService;

        this.persistentWSCache = CacheBuilder.newBuilder()
                                             .expireAfterAccess(persistentTime,
                                                                TimeUnit.MILLISECONDS)
                                             .removalListener(removalListener)
                                             .build();

        this.temporaryWSCache = CacheBuilder.newBuilder()
                                            .expireAfterAccess(temporaryTime, TimeUnit.MILLISECONDS)
                                            .removalListener(removalListener)
                                            .build();
    }

    public void onMessage(String workspaceId, boolean isTemporary) {
        if (isTemporary) {
            if (temporaryWSCache.getIfPresent(workspaceId) == null) {
                temporaryWSCache.put(workspaceId, true);
            }
        } else if (persistentWSCache.getIfPresent(workspaceId) == null) {
            persistentWSCache.put(workspaceId, false);
        }
        temporaryWSCache.cleanUp();
        persistentWSCache.cleanUp();
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(new EventSubscriber<WsActivityEvent>() {
            @Override
            public void onEvent(WsActivityEvent event) {
                onMessage(event.getWorkspaceId(), event.isTemporary());
            }
        });
    }
}
