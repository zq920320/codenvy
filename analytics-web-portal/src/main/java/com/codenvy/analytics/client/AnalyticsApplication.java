/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

public class AnalyticsApplication {

    public void onModuleLoad() {

        QueryServiceAsync queryService = GWT.create(QueryService.class);
        TimeLineViewServiceAsync viewService = GWT.create(TimeLineViewService.class);

        HandlerManager eventBus = new HandlerManager(null);
        AppController appViewer = new AppController(queryService, viewService, eventBus);
        appViewer.go(RootPanel.get());
    }
}
