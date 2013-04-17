package com.codenvy.analytics.client;


import com.codenvy.analytics.client.view.TimeLineWidget;
import com.codenvy.analytics.shared.DataView;
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
    private final TimeLineViewServiceAsync viewService = GWT.create(TimeLineViewService.class);

    /** This is the entry point method. */
    public void onModuleLoad() {
        viewService.getViews(new Date(), new AsyncCallback<List<DataView>>() {
            public void onFailure(Throwable caught) {
                Window.alert("Something gone wrong. See server logs for details " + caught.getMessage());
            }

            public void onSuccess(List<DataView> result) {
                for (DataView data : result) {
                    RootPanel.get("timeLineContainer").add(TimeLineWidget.createWidget(data.iterator()));
                }
            }
        });
    }
}
