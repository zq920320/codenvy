package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.AnalyticsApplication;
import com.codenvy.analytics.server.view.TimeLineViewServer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.Iterator;
import java.util.List;


/**
 * @author <a href="abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimelineView extends Composite
{
    private final AnalyticsApplication portal;

    private final FlexTable            flexTableMain = new FlexTable();

    public TimelineView(final AnalyticsApplication portal) {
        this.portal = portal;

        flexTableMain.setStyleName("flexTableMain");

        // portal.getViewService().getTimeLineView(new Date(), new AsyncCallback<List<List<String>>>() {
        // public void onFailure(Throwable caught) {
        // Window.alert("Something gone wrong. See server logs for details");
        // flexTableMain.setText(0, 0, caught.getMessage());
        // }
        //
        // public void onSuccess(List<List<String>> result) {
        // flexTableMain.setWidget(0, 0, createWidget(result.iterator()));
        // }
        // });

        initWidget(flexTableMain);
    }

    public static Widget createWidget(Iterator<List<String>> iter) {
        CellTable<List<String>> timeline = new CellTable<List<String>>();

        createsColumns(timeline);

        ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>();
        dataProvider.addDataDisplay(timeline);

        List<List<String>> list = dataProvider.getList();
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return timeline;
    }

    private static void createsColumns(CellTable<List<String>> timeline) {
        for (int i = 0; i < TimeLineViewServer.HISTORY_LENGTH; i++) {
            CustomColumn column = new CustomColumn(i);
            timeline.addColumn(column);
        }
    }

    static class CustomColumn extends TextColumn<List<String>> {
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
