package com.codenvy.analytics.client.view.query;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Map;

class MetricTypesAsyncCallback implements AsyncCallback<Map<String, String>> {
    private final QueryViewImpl queryView;

    public MetricTypesAsyncCallback(QueryViewImpl queryView) {
        super();
        this.queryView = queryView;
    }

    public void onSuccess(final Map<String, String> result) {
        queryView.setMetrics(result);
        queryView.getParameterPanel().init();
        queryView.getMainFlexTable().setWidget(0, 0, queryView.getParameterPanel());
    }

    public void onFailure(Throwable caught) {
        queryView.getMainFlexTable().setText(0, 0, caught.getMessage());
    }
}