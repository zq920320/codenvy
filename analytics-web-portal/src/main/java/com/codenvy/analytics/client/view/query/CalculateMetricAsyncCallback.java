package com.codenvy.analytics.client.view.query;

import com.google.gwt.user.client.rpc.AsyncCallback;

class CalculateMetricAsyncCallback implements AsyncCallback<String> {
    private final QueryViewImpl queryView;

    CalculateMetricAsyncCallback(QueryViewImpl queryView) {
        this.queryView = queryView;
    }

    public void onSuccess(String result) {
        queryView.getGwtLoader().hide();
        queryView.getFlexTableMain().setText(3, 0, result);
    }

    public void onFailure(Throwable caught) {
        queryView.getGwtLoader().hide();
        queryView.getFlexTableMain().setText(3, 0, caught.getMessage());
    }
}