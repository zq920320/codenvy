package com.codenvy.analytics.client;

import com.codenvy.analytics.client.view.TimeLineWidget;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.Date;
import java.util.List;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class AnalyticsApplication implements EntryPoint {

    /** Create a remote service proxy. */
    private final ViewServiceAsync viewService = GWT.create(ViewService.class);

    /** This is the entry point method. */
    public void onModuleLoad() {
        viewService.getTimeLineView(new Date(), new AsyncCallback<List<List<String>>>() {
            public void onFailure(Throwable caught) {
                Window.alert("Something gone wrong. See server logs for details");
            }

            public void onSuccess(List<List<String>> result) {
                RootPanel.get("timeLineContainer").add(TimeLineWidget.createWidget(result.iterator()));
            }
        });
    }
}
