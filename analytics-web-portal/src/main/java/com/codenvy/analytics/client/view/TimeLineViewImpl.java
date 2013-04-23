/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.AnalyticsApplication;
import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.TimeLineViewServiceAsync;
import com.codenvy.analytics.client.resources.GWTCellTableResource;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.shared.TimeLineViewData;
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
    private final FlexTable                table = new FlexTable();
    private final GWTLoader                gwtLoader = new GWTLoader();
    private final TimeLineViewServiceAsync viewService;

    /**
     * {@link TimeLineViewImpl} constructor.
     */
    public TimeLineViewImpl(final AnalyticsApplication portal) {
        this.viewService = portal.getViewService();
        update(portal.getCurrentTimeUnit());
    }

    /**
     * {@inheritDoc}
     */
    public void update(TimeUnit timeUnit) {
        table.clear();
        gwtLoader.show();

        viewService.getViews(new Date(), timeUnit, new AsyncCallback<List<TimeLineViewData>>() {
            public void onFailure(Throwable caught) {
                gwtLoader.hide();
                table.setText(0, 0, caught.getMessage());
            }

            public void onSuccess(List<TimeLineViewData> result) {
                gwtLoader.hide();
                for (int i = 0; i < result.size(); i++) {
                    Iterator<List<String>> rowIterator = result.get(i).iterator();
                    table.setWidget(i, 0, createCellTableWidget(rowIterator));
                }
            }
        });

        initWidget(table);
    }

    private Widget createCellTableWidget(Iterator<List<String>> rowIterator) {
        GWTCellTableResource resources = GWT.create(GWTCellTableResource.class);

        CellTable<List<String>> timeline = new CellTable<List<String>>(Integer.MAX_VALUE, resources);
        ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>();
        dataProvider.addDataDisplay(timeline);

        List<String> headers = rowIterator.next();
        createsColumns(timeline, headers);

        List<List<String>> list = dataProvider.getList();
        while (rowIterator.hasNext()) {
            list.add(rowIterator.next());
        }

        return timeline;
    }


    private void createsColumns(CellTable<List<String>> timeline, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            CustomColumn column = new CustomColumn(i);
            timeline.addColumn(column, headers.get(i));
        }
    }

    private class CustomColumn extends TextColumn<List<String>> {
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
