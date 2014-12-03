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
package com.codenvy.workspace.event;

import com.codenvy.api.workspace.server.dao.Workspace;

/** @author Sergii Leschenko */
public abstract class WorkspaceEvent {
    public static enum ChangeType {
        CREATED("created"),
        DELETED("deleted");

        private final String value;

        private ChangeType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    private final ChangeType type;
    private final Workspace  workspace;

    protected WorkspaceEvent(ChangeType type, Workspace workspace) {
        this.type = type;
        this.workspace = workspace;
    }

    public ChangeType getType() {
        return type;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
}
