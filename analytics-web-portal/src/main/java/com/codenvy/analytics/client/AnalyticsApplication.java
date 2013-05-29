/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalyticsApplication {

    public void onModuleLoad() {
        QueryServiceAsync queryService = GWT.create(QueryService.class);
        TimeLineViewServiceAsync viewService = GWT.create(TimeLineViewService.class);
        SingupAnalysisViewServiceAsync singupService = GWT.create(SingupAnalysisViewService.class);

        HandlerManager eventBus = new HandlerManager(null);
        AppController appViewer = new AppController(queryService, viewService, singupService, eventBus);
        appViewer.go(RootPanel.get());
    }
}
