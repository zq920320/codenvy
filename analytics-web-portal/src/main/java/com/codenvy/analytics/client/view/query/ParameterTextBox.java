package com.codenvy.analytics.client.view.query;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

class ParameterTextBox extends TextBox {
    private final QueryViewImpl queryVoew;

    public ParameterTextBox(QueryViewImpl queryImpl, final String parameterName, String dafaultValue) {
        super();
        this.queryVoew = queryImpl;
        this.queryVoew.getSelectedMetricParameters().put(parameterName, dafaultValue);

        this.setText(dafaultValue);

        this.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                queryVoew.getSelectedMetricParameters().put(parameterName, event.getValue());
            }
        });
    }
}