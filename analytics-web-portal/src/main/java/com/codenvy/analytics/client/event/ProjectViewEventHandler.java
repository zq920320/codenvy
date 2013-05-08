package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface ProjectViewEventHandler extends EventHandler {
    void onLoad(ProjectViewEvent event);
}
