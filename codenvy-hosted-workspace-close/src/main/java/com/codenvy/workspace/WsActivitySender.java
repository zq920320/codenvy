/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.workspace;

import com.codenvy.api.core.notification.EventService;
import com.codenvy.workspace.event.WsActivityEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Send event about activity in workspace, but not more often than once per minute.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WsActivitySender {
    public static final long ACTIVITY_PERIOD = 60000L;
    private EventService eventService;

    private final ConcurrentMap<String, Entry> wsCache;

    @Inject
    public WsActivitySender(EventService eventService) {
        this.eventService = eventService;
        wsCache = new ConcurrentHashMap<>(100);
    }

    public void onMessage(String workspaceId, boolean isTemporary) {
        Entry entry = wsCache.get(workspaceId);

        if (null == entry || entry.getTimestamp() < System.currentTimeMillis()) {
            long acP = System.currentTimeMillis() + ACTIVITY_PERIOD;
            wsCache.put(workspaceId, new Entry(acP, isTemporary));
            eventService.publish(new WsActivityEvent(workspaceId, isTemporary));
        }
    }

    class Entry {
        private long    timestamp;
        private boolean isTemporary;

        public Entry(long timestamp, boolean isTemporary) {
            this.timestamp = timestamp;
            this.isTemporary = isTemporary;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isTemporary() {
            return isTemporary;
        }

        public void setTemporary(boolean isTemporary) {
            this.isTemporary = isTemporary;
        }
    }
}
