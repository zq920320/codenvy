/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface SingupAnalysisViewEventHandler extends EventHandler {
    void onLoad(SingupAnalysisViewEvent event);
}
