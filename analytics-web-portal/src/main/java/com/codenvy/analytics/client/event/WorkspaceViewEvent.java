package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

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
