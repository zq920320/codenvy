/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
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
