/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.AnalyticsApplication;
import com.codenvy.analytics.client.TimeLineViewServiceAsync;
import com.codenvy.analytics.shared.DataView;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

import java.util.Date;
import java.util.List;


/**
 * @author <a href="abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineView extends Composite
{
    private final FlexTable            flexTableMain = new FlexTable();

    public TimeLineView(final AnalyticsApplication portal) {
        flexTableMain.setStyleName("flexTableMain");

        TimeLineViewServiceAsync viewService = portal.getViewService();
        viewService.getViews(new Date(), new AsyncCallback<List<DataView>>() {
            public void onFailure(Throwable caught) {
                flexTableMain.setText(0, 0, caught.getMessage());
            }

            public void onSuccess(List<DataView> result) {
                for (int i = 0; i < result.size(); i++) {
                    flexTableMain.setWidget(i, 0, TimeLineWidget.createWidget(result.get(i).iterator()));
                }
            }
        });

        initWidget(flexTableMain);
    }
}
