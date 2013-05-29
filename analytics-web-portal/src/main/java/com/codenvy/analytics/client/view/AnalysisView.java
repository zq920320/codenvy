/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.AnalysisViewPresenter;
import com.codenvy.analytics.client.resources.GWTCellTableResource;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalysisView extends MainView implements AnalysisViewPresenter.Display {

    private final FlexTable contentTable;

    public AnalysisView() {
        super();

        this.contentTable = new FlexTable();
        getMainPanel().add(contentTable);
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

    private Widget createCellTableWidget(Iterator<List<String>> rowIterator) {
        GWTCellTableResource resources = GWT.create(GWTCellTableResource.class);

        CellTable<List<String>> table = new CellTable<List<String>>(Integer.MAX_VALUE, resources);
        ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>();
        dataProvider.addDataDisplay(table);

        List<String> headers = rowIterator.next();
        createsColumns(table, headers);

        List<List<String>> list = dataProvider.getList();
        while (rowIterator.hasNext()) {
            list.add(rowIterator.next());
        }

        return table;
    }

    private void createsColumns(CellTable<List<String>> table, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            CustomColumn column = new CustomColumn(i);
            table.addColumn(column, headers.get(i));
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

    /** {@inheritDoc} */
    @Override
    public GWTLoader getGWTLoader() {
        return super.getGwtLoader();
    }

    /** {@inheritDoc} */
    @Override
    public void setData(List<TimeLineViewData> result) {
        for (int i = 0; i < result.size(); i++) {
            Iterator<List<String>> rowIterator = result.get(i).iterator();
            getContentTable().setWidget(i, 0, createCellTableWidget(rowIterator));
        }
    }

    /** {@inheritDoc} */
    @Override
    public FlexTable getContentTable() {
        return contentTable;
    }
}
