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
package com.codenvy.api.account;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Workspace locking/unlocking event.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("workspacelock")
public class WorkspaceLockEvent {
    public enum EventType {
        WORKSPACE_LOCKED("workspace locked"),
        WORKSPACE_UNLOCKED("workspace unlocked");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private EventType type;
    private String    workspaceId;

    public WorkspaceLockEvent(EventType type, String accountId) {
        this.type = type;
        this.workspaceId = accountId;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setAccount(String account) {
        this.workspaceId = account;
    }

    public static WorkspaceLockEvent workspaceLockedEvent(String accountId) {
        return new WorkspaceLockEvent(EventType.WORKSPACE_LOCKED, accountId);
    }

    public static WorkspaceLockEvent workspaceUnlockedEvent(String accountId) {
        return new WorkspaceLockEvent(EventType.WORKSPACE_UNLOCKED, accountId);
    }

    @Override
    public String toString() {
        return "WorkspaceLockEvent{" +
               "type=" + type +
               ", accountId='" + workspaceId + '\'' +
               '}';
    }
}