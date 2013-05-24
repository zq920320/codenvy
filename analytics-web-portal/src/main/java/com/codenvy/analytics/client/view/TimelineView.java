package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.TimelineViewPresenter;
import com.codenvy.analytics.client.resources.GWTCellTableResource;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.Iterator;
import java.util.List;

public class TimelineView extends MainView implements TimelineViewPresenter.Display {

    private final TextBox   userNameFilter    = new TextBox();
    private final Button    applyFilterButton = new Button("Go");
    private final FlexTable contentTable      = new FlexTable();
    private final ListBox   timeUnitBox       = new ListBox();

    public TimelineView() {
        super();

        HorizontalPanel timeUnitPanel = new HorizontalPanel();
        Label label = new Label("Time Unit:");
        label.getElement().setAttribute("align", "middle");
        timeUnitPanel.add(new Label("Time Unit: "));
        timeUnitPanel.add(timeUnitBox);
        timeUnitBox.getElement().setAttribute("align", "right");

        HorizontalPanel domainPanel = new HorizontalPanel();
        domainPanel.add(new Label("Users email: "));
        domainPanel.add(userNameFilter);
        domainPanel.add(applyFilterButton);

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(timeUnitPanel);
        verticalPanel.add(domainPanel);

        getSubHeaderPanel().add(verticalPanel);
        for (TimeUnit timeUnit : TimeUnit.values()) {
            timeUnitBox.addItem(timeUnit.toString().toLowerCase());
        }

        timeUnitBox.setVisibleItemCount(1);
        getMainPanel().add(contentTable);
    }

    public Widget asWidget() {
        return this;
    }

    public FlexTable getContentTable() {
        return contentTable;
    }

    public ListBox getTimeUnitBox() {
        return timeUnitBox;
    }

    public String getUserFilter() {
        return userNameFilter.getText();
    }

    public Button getApplyFilterButton() {
        return applyFilterButton;
    }

    public void setData(List<TimeLineViewData> result) {
        for (int i = 0; i < result.size(); i++) {
            Iterator<List<String>> rowIterator = result.get(i).iterator();
            getContentTable().setWidget(i, 0, createCellTableWidget(rowIterator));
        }
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

    public GWTLoader getGWTLoader() {
        return super.getGwtLoader();
    }
}
