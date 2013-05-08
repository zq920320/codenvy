package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ProjectViewEvent extends GwtEvent<ProjectViewEventHandler> {
    public static Type<ProjectViewEventHandler> TYPE = new Type<ProjectViewEventHandler>();

    @Override
    public Type<ProjectViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ProjectViewEventHandler handler) {
        handler.onLoad(this);
    }
}
