package com.codenvy.analytics.client.view.query;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

import java.util.ArrayList;
import java.util.List;

class MetricParametersAsyncCallback implements AsyncCallback<List<ArrayList<String>>> {
    private final QueryViewImpl queryView;
    private final FlexTable     flexTable;

    public MetricParametersAsyncCallback(QueryViewImpl queryView) {
        super();
        this.queryView = queryView;
        this.flexTable = queryView.getParameterPanel().getParameterFlexTable();
    }

    public void onFailure(Throwable caught) {
        flexTable.setText(0, 0, caught.getMessage());
    }

    public void onSuccess(List<ArrayList<String>> result) {
        flexTable.removeAllRows();
        int rowCounter = 0;

        for (final List<String> parameterEntry : result) {
            flexTable.setText(rowCounter, 0, parameterEntry.get(3));
            if ("fromDate".equals(parameterEntry.get(0))
                || "toDate".equals(parameterEntry.get(0))) {
                flexTable.setWidget(rowCounter, 1, new ParameterDateBox(queryView, parameterEntry.get(0), parameterEntry.get(1)));
            }
            else {
                flexTable.setWidget(rowCounter,
                                    1,
                                    new ParameterTextBox(queryView,
                                                         parameterEntry.get(0),
                                                         parameterEntry.get(1)));
            }
            flexTable.setText(rowCounter, 2, parameterEntry.get(2));

            rowCounter++;
        }

    }
}
