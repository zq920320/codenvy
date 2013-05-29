/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UserViewEvent extends GwtEvent<UserViewEventHandler> {
    public static Type<UserViewEventHandler> TYPE = new Type<UserViewEventHandler>();

    @Override
    public Type<UserViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UserViewEventHandler handler) {
        handler.onLoad(this);
    }
}
