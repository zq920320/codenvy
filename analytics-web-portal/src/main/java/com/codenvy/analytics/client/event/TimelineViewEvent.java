package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

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
