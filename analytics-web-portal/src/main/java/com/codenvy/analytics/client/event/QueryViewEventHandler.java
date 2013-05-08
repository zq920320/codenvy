package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface QueryViewEventHandler extends EventHandler {
    void onLoad(QueryViewEvent event);
}
