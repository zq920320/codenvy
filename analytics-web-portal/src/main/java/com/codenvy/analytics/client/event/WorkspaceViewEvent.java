/*
 *    Copyright (C) 2013 Codenvy.
 *
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
