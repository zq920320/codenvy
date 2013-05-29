/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalysisViewEvent extends GwtEvent<AnalysisViewEventHandler> {
    public static Type<AnalysisViewEventHandler> TYPE = new Type<AnalysisViewEventHandler>();

    @Override
    public Type<AnalysisViewEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(AnalysisViewEventHandler handler) {
        handler.onLoad(this);
    }
}
