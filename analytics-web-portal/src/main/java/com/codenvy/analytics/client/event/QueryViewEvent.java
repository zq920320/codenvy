package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class QueryViewEvent extends GwtEvent<QueryViewEventHandler> {
    public static Type<QueryViewEventHandler> TYPE = new Type<QueryViewEventHandler>();

    @Override
    public Type<QueryViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(QueryViewEventHandler handler) {
        handler.onLoad(this);
    }
}
