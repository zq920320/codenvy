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

import com.codenvy.api.core.notification.EventOrigin;

/** @author Sergii Leschenko */
@EventOrigin("workspace")
public class DeleteWorkspaceEvent extends WorkspaceEvent {
    private String name;

    public DeleteWorkspaceEvent(String workspaceId, boolean temporary, String name) {
        super(workspaceId, temporary, ChangeType.DELETED);
        this.name = name;
    }

    public DeleteWorkspaceEvent() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
