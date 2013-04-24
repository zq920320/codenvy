package com.codenvy.analytics.client.view.query;

import com.codenvy.analytics.client.AnalyticsApplication;
import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.client.view.View;
import com.codenvy.analytics.metrics.TimeUnit;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;

import java.util.HashMap;
import java.util.Map;

public class QueryViewImpl extends Composite implements View {
    private AnalyticsApplication portal;
    private Map<String, String>  metrics;
    private String               selectedMetricTitle;
    private Map<String, String>  currentMetricParameters = new HashMap<String, String>();
    private GWTLoader            gwtLoader               = new GWTLoader();
    private FlexTable            mainFlexTable           = new FlexTable();
    private ParameterPanel       parameterPanel          = new ParameterPanel(this);
    private RunQueryButton       runQueryButton          = new RunQueryButton("Run Query", this);

    public QueryViewImpl(final AnalyticsApplication portal) {
        this.portal = portal;
        initWidget(mainFlexTable);

        mainFlexTable.setStyleName("flexTableMain");

        this.portal.getQueryService().getMetricTypes(new MetricTypesAsyncCallback(this));

        mainFlexTable.setWidget(1, 0, runQueryButton);
    }

    /**
     * {@inheritDoc}
     */
    public void update(TimeUnit timeUnit) {
        // do nothing
    }

    public Map<String, String> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }

    public GWTLoader getGwtLoader() {
        return gwtLoader;
    }

    public AnalyticsApplication getPortal() {
        return portal;
    }

    public String getSelectedMetricTitle() {
        return selectedMetricTitle;
    }

    public void setSelectedMetricTitle(String selectedMetricTitle) {
        this.selectedMetricTitle = selectedMetricTitle;
    }

    public String getSelectedMetricName() {
        return metrics.get(selectedMetricTitle);
    }

    public Map<String, String> getSelectedMetricParameters() {
        return currentMetricParameters;
    }

    public RunQueryButton getRunQueryButton() {
        return runQueryButton;
    }

    public void setRunQueryButton(RunQueryButton runQueryButton) {
        this.runQueryButton = runQueryButton;
    }

    public FlexTable getFlexTableMain() {
        return mainFlexTable;
    }

    public FlexTable getMainFlexTable() {
        return mainFlexTable;
    }

    public void setMainFlexTable(FlexTable mainFlexTable) {
        this.mainFlexTable = mainFlexTable;
    }

    public ParameterPanel getParameterPanel() {
        return parameterPanel;
    }

    public void setParameterPanel(ParameterPanel parameterPanel) {
        this.parameterPanel = parameterPanel;
    }

    public Map<String, String> getCurrentMetricParameters() {
        return currentMetricParameters;
    }

}
