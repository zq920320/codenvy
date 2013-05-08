package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface TimelineViewEventHandler extends EventHandler {
    void onLoad(TimelineViewEvent event);
}
