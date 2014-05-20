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
package com.codenvy.workspace.event;

import com.codenvy.api.core.notification.EventOrigin;

/**
 * Inform that there is activity in certain workspace.
 *
 * @author Alexander Garagatyi
 */
@EventOrigin("ws-activity")
public class WsActivityEvent {
    private String  workspaceId;
    private boolean isTemporary;

    public WsActivityEvent() {
    }

    public WsActivityEvent(String id, boolean isTemporary) {
        this.workspaceId = id;
        this.isTemporary = isTemporary;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }
}
