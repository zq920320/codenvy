package com.codenvy.analytics.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface UserViewEventHandler extends EventHandler {
    void onLoad(UserViewEvent event);
}
