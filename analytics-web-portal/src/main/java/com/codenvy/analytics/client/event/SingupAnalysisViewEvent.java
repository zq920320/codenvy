/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class SingupAnalysisViewEvent extends GwtEvent<SingupAnalysisViewEventHandler> {
    public static Type<SingupAnalysisViewEventHandler> TYPE = new Type<SingupAnalysisViewEventHandler>();

    @Override
    public Type<SingupAnalysisViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SingupAnalysisViewEventHandler handler) {
        handler.onLoad(this);
    }
}
