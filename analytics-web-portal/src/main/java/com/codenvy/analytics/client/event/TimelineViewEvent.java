/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimelineViewEvent extends GwtEvent<TimelineViewEventHandler> {
    public static Type<TimelineViewEventHandler> TYPE = new Type<TimelineViewEventHandler>();

    @Override
    public Type<TimelineViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TimelineViewEventHandler handler) {
        handler.onLoad(this);
    }
}
