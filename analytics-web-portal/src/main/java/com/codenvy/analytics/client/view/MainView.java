/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.presenter.MainViewPresenter;
import com.codenvy.analytics.client.resources.GWTCellTableResource;
import com.codenvy.analytics.client.resources.GWTDataGridResource;
import com.codenvy.analytics.shared.RowData;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class MainView extends Composite implements MainViewPresenter.Display {

    private final GWTLoader       gwtLoader     = new GWTLoader();

    private final Button          timelineViewButton;
    private final Button          analysisButton;
    private final Button          workspaceViewButton;
    private final Button          userViewButton;
    private final Button          projectViewButton;
    private final Button          queryViewButton;

    private final FlexTable       contentTable   = new FlexTable();

    private final VerticalPanel   mainPanel      = new VerticalPanel();
    private final HorizontalPanel headerPanel    = new HorizontalPanel();
    private final VerticalPanel   subHeaderPanel = new VerticalPanel();

    public MainView() {
        timelineViewButton = new Button("Time Line");
        analysisButton = new Button("Analysis");
        workspaceViewButton = new Button("Workspace");
        userViewButton = new Button("User");
        projectViewButton = new Button("Project");
        queryViewButton = new Button("Query");

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(timelineViewButton);
        hp.add(analysisButton);
        hp.add(workspaceViewButton);
        hp.add(userViewButton);
        hp.add(projectViewButton);
        hp.add(queryViewButton);
        hp.getElement().setAttribute("align", "center");

        mainPanel.setWidth("100%");

        headerPanel.add(hp);
        headerPanel.setWidth("100%");
        headerPanel.getElement().setAttribute("align", "middle");

        subHeaderPanel.setWidth("100%");
        subHeaderPanel.getElement().setAttribute("align", "middle");

        contentTable.getElement().setAttribute("align", "center");
        contentTable.setWidth("100%");

        mainPanel.add(headerPanel);
        mainPanel.add(subHeaderPanel);
        mainPanel.add(contentTable);
        mainPanel.getElement().setAttribute("align", "center");

        initWidget(mainPanel);
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Button getTimelineViewButton() {
        return timelineViewButton;
    }

    /** {@inheritDoc} */
    @Override
    public Button getWorkspaceViewButton() {
        return workspaceViewButton;
    }

    /** {@inheritDoc} */
    @Override
    public Button getUserViewButton() {
        return userViewButton;
    }

    /** {@inheritDoc} */
    @Override
    public Button getProjectViewButton() {
        return projectViewButton;
    }

    /** {@inheritDoc} */
    @Override
    public HasClickHandlers getAnalysisViewButton() {
        return analysisButton;
    }

    /** {@inheritDoc} */
    @Override
    public Button getQueryViewButton() {
        return queryViewButton;
    }

    /** {@inheritDoc} */
    @Override
    public VerticalPanel getMainPanel() {
        return mainPanel;
    }

    /** {@inheritDoc} */
    @Override
    public HorizontalPanel getHeaderPanel() {
        return headerPanel;
    }

    /** {@inheritDoc} */
    @Override
    public VerticalPanel getSubHeaderPanel() {
        return subHeaderPanel;
    }

    /** {@inheritDoc} */
    @Override
    public GWTLoader getGWTLoader() {
        return gwtLoader;
    }

    /** {@inheritDoc} */
    @Override
    public FlexTable getContentTable() {
        return contentTable;
    }

    /** {@inheritDoc} */
    @Override
    public void setErrorMessage(String message) {
        getContentTable().clear();
        getContentTable().setText(0, 0, message);
    }

    /** {@inheritDoc} */
    @Override
    public void setData(List<TableData> result) {
        getContentTable().clear();

        Map<String, TabLayoutPanel> tabs = new HashMap<String, TabLayoutPanel>();

        for (int i = 0; i < result.size(); i++) {
            TableData tableData = result.get(i);

            switch (tableData.getWidget()) {
                case CELL_TABLE:
                    Widget widget = createCellTable(tableData);
                    getContentTable().setWidget(i, 0, widget);
                    break;

                case TAB_PANEL:
                    String tabId = tableData.getTabId();

                    if (!tabs.containsKey(tabId)) {
                        TabLayoutPanel tabPanel = createTabPanel();
                        tabs.put(tabId, tabPanel);

                        getContentTable().setWidget(i, 0, tabPanel);
                    }

                    TabLayoutPanel tabPanel = tabs.get(tabId);
                    tabPanel.add(createDataGrid(tableData), tableData.getTitle());
                    break;

                default:
                    continue;
            }
        }

        for (TabLayoutPanel panel : tabs.values()) {
            panel.selectTab(0);
        }
    }

    protected Widget createCellTable(TableData tableData) {
        CellTable<RowData> cellTable = new CellTable<RowData>(Integer.MAX_VALUE, GWTCellTableResource.RESOURCES);
        return initializeTable(cellTable, tableData);
    }

    protected Widget createDataGrid(TableData tableData) {
        DataGrid<RowData> dataGrid = new DataGrid<RowData>(Integer.MAX_VALUE, GWTDataGridResource.RESOURCES);
        return initializeTable(dataGrid, tableData);
    }

    protected Widget initializeTable(AbstractCellTable<RowData> cellTable, TableData tableData) {
        cellTable.getElement().setAttribute("align", "center");

        ListDataProvider<RowData> dataProvider = new ListDataProvider<RowData>();
        dataProvider.addDataDisplay(cellTable);

        List<RowData> list = dataProvider.getList();

        Iterator<RowData> rowDataIterator = tableData.iterator();

        createColumns(cellTable, rowDataIterator.next());
        addContent(list, rowDataIterator);

        if (tableData.isSortable()) {
            addSortHandler(cellTable, tableData, dataProvider);
        }

        return cellTable;
    }

    private void addContent(List<RowData> list, Iterator<RowData> rowDataIterator) {
        while (rowDataIterator.hasNext()) {
            list.add(rowDataIterator.next());
        }
    }

    private void addSortHandler(AbstractCellTable<RowData> table, TableData tableData, ListDataProvider<RowData> dataProvider) {
        ListHandler<RowData> sortHandler = new ListHandler<RowData>(dataProvider.getList());

        CustomColumn defaultSortColumn = null;

        for (int i = 0; i < table.getColumnCount(); i++) {
            final CustomColumn column = (CustomColumn)table.getColumn(i);
            column.setSortable(true);
            column.setDefaultSortAscending(false);

            if (column.name.equals(tableData.getDefaultSortColumn())) {
                defaultSortColumn = column;
            }

            sortHandler.setComparator(column, new DataComparator(column.number, false));
        }

        table.addColumnSortHandler(sortHandler);

        if (defaultSortColumn != null) {
            Collections.sort(dataProvider.getList(), new DataComparator(defaultSortColumn.number, true));
            table.getColumnSortList().push(defaultSortColumn);
        }
    }

    protected void createColumns(AbstractCellTable<RowData> table, RowData headers) {
        for (int i = 0; i < headers.size(); i++) {
            CustomColumn column = new CustomColumn(i, headers.get(i));
            table.addColumn(column, headers.get(i));
        }
    }


    private class CustomColumn extends TextColumn<RowData> {
        private final String name;
        private final int    number;

        public CustomColumn(int number, String name) {
            this.number = number;
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue(RowData object) {
            return object.get(number);
        }
    }

    protected TabLayoutPanel createTabPanel() {
        TabLayoutPanel tabPanel = new TabLayoutPanel(25, Unit.PX);
        tabPanel.setSize("90%", "400px");
        
        // workaround for DataGrid issue
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                TabLayoutPanel panel = (TabLayoutPanel)event.getSource();
                panel.onResize();
            }
        });

        return tabPanel;
    }

    private final class DataComparator implements Comparator<RowData> {
        private final int index;
        private final int rate;

        private DataComparator(int index, boolean reverseOrder) {
            this.index = index;
            this.rate = reverseOrder ? -1 : 1;
        }

        /**
         * Comparator. First tries to compare value as Long and than as String.
         */
        @Override
        public int compare(RowData o1, RowData o2) {
            String value1 = o1.get(index);
            String value2 = o2.get(index);

            try {
                Long longV1 = Long.valueOf(value1);
                Long longV2 = Long.valueOf(value2);
                return longV1.compareTo(longV2) * rate;
            } catch (NumberFormatException e) {
                return value1.compareTo(value2) * rate;
            }
        }
    }
}
