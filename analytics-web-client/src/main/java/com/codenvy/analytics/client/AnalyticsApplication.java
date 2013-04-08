package com.codenvy.analytics.client;

import com.codenvy.analytics.client.view.TimeLineWidget;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class AnalyticsApplication implements EntryPoint {

    /** Create a remote service proxy to talk to the server-side Greeting service. */
//    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

    /** This is the entry point method. */
    public void onModuleLoad() {
        RootPanel.get("timeLineContainer").add(TimeLineWidget.createWidget());
    }
}
