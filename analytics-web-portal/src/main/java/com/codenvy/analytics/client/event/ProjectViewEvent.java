/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
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
