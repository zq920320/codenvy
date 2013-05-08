package com.codenvy.analytics.client.resources;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

import java.util.Map;

public class ParameterTextBox extends TextBox {

    public ParameterTextBox(final Map<String, String> selectedParameters, final String parameterName, String dafaultValue) {
        super();
        this.setText(dafaultValue);

        selectedParameters.put(parameterName, dafaultValue);

        this.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                selectedParameters.put(parameterName, event.getValue());
            }
        });
    }
}