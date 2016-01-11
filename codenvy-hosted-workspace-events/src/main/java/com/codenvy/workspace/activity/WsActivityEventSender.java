/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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

import com.codenvy.workspace.event.WsActivityEvent;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.eclipse.che.api.core.notification.EventService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Send event about activity in workspace, but not more often than once per minute.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 */
@Singleton
public class WsActivityEventSender {
    public static final long ACTIVITY_PERIOD = 60000L;
    private EventService eventService;

    private final Cache<String, Long> wsId2LastAccessTime;

    @Inject
    public WsActivityEventSender(EventService eventService) {
        this.eventService = eventService;
        wsId2LastAccessTime = CacheBuilder.newBuilder()
                                          .expireAfterWrite(10, TimeUnit.MINUTES)
                                          .build();
    }

    public void onActivity(String workspaceId, boolean isTemporary) {
        Long lastAccessTime = wsId2LastAccessTime.getIfPresent(workspaceId);

        if (null == lastAccessTime || lastAccessTime < System.currentTimeMillis()) {
            wsId2LastAccessTime.put(workspaceId, System.currentTimeMillis() + ACTIVITY_PERIOD);
            eventService.publish(new WsActivityEvent(workspaceId, isTemporary));
        }
    }
}
