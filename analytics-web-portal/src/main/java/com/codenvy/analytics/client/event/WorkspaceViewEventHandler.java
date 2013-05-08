package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface WorkspaceViewEventHandler extends EventHandler {
    void onLoad(WorkspaceViewEvent event);
}
