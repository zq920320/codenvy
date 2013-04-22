/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.resources.GWTCellTableResource;

import com.codenvy.analytics.client.AnalyticsApplication;
import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.TimeLineViewServiceAsync;
import com.codenvy.analytics.shared.DataView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * @author <a href="abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineViewImpl extends Composite implements View {
    private final FlexTable            flexTableMain = new FlexTable();

    public TimeLineViewImpl(final AnalyticsApplication portal) {
        flexTableMain.setStyleName("flexTableMain");

        final GWTLoader gwtLoader = new GWTLoader();
        gwtLoader.show();

        TimeLineViewServiceAsync viewService = portal.getViewService();
        viewService.getViews(new Date(), new AsyncCallback<List<DataView>>() {
            public void onFailure(Throwable caught) {
                gwtLoader.hide();
                flexTableMain.setText(0, 0, caught.getMessage());
            }

            public void onSuccess(List<DataView> result) {
                gwtLoader.hide();
                for (int i = 0; i < result.size(); i++) {
                    flexTableMain.setWidget(i, 0, createWidget(result.get(i).iterator()));
                }
            }
        });

        initWidget(flexTableMain);
    }
    
    private Widget createWidget(Iterator<List<String>> iter) {
        GWTCellTableResource resources = GWT.create(GWTCellTableResource.class);

        CellTable<List<String>> timeline = new CellTable<List<String>>(Integer.MAX_VALUE, resources);
        ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>();
        dataProvider.addDataDisplay(timeline);

        List<String> row = iter.next();
        createsColumns(timeline, row);

        List<List<String>> list = dataProvider.getList();
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return timeline;
    }


    private void createsColumns(CellTable<List<String>> timeline, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            CustomColumn column = new CustomColumn(i);
            timeline.addColumn(column, headers.get(i));
        }
    }

    class CustomColumn extends TextColumn<List<String>> {
        private final int number;

        public CustomColumn(int number) {
            this.number = number;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue(List<String> object) {
            return object.get(number);
        }
    }
}
