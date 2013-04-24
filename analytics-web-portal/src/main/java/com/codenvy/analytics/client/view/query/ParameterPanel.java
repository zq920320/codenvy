package com.codenvy.analytics.client.view.query;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

public class ParameterPanel extends VerticalPanel {
    final private QueryViewImpl queryView;
    private ParameterListBox    parameterListBox;
    private ParameterFlexTable  parameterFlexTable;

    public ParameterPanel(QueryViewImpl queryView) {
        super();
        this.queryView = queryView;
    }

    public void init()
    {
        parameterListBox = new ParameterListBox();
        parameterFlexTable = new ParameterFlexTable();

        add(parameterListBox);
        add(parameterFlexTable);
        parameterFlexTable.refresh();
    }

    private class ParameterListBox extends ListBox {
        public ParameterListBox() {
            super();

            for (String metricTitle : getMetricTitles()) {
                this.addItem(metricTitle);
            }

            this.setVisibleItemCount(1);
            this.addChangeHandler(new ParameterListBoxChangeHandler());
        }

    }

    private class ParameterFlexTable extends FlexTable {
        public void refresh()
        {
            queryView.setSelectedMetricTitle(getMetricTitles().get(parameterListBox.getSelectedIndex()));
            queryView.getPortal().getQueryService().getMetricParametersList(queryView.getSelectedMetricName(),
                                                                            new MetricParametersAsyncCallback(queryView));
        }

    }

    private class ParameterListBoxChangeHandler implements ChangeHandler {
        public void onChange(ChangeEvent event) {
            parameterFlexTable.refresh();
        }
    }


    private List<String> getMetricTitles() {
        return new ArrayList<String>(queryView.getMetrics().keySet());
    }

    public ParameterListBox getParameterListBox() {
        return parameterListBox;
    }

    public void setParameterListBox(ParameterListBox parameterListBox) {
        this.parameterListBox = parameterListBox;
    }

    public ParameterFlexTable getParameterFlexTable() {
        return parameterFlexTable;
    }

    public void setParameterFlexTable(ParameterFlexTable parameterFlexTable) {
        this.parameterFlexTable = parameterFlexTable;
    }

    public QueryViewImpl getQueryView() {
        return queryView;
    }
}
