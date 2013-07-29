/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspaceViewEvent extends GwtEvent<WorkspaceViewEventHandler> {
    public static Type<WorkspaceViewEventHandler> TYPE = new Type<WorkspaceViewEventHandler>();

    @Override
    public Type<WorkspaceViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(WorkspaceViewEventHandler handler) {
        handler.onLoad(this);
    }
}
