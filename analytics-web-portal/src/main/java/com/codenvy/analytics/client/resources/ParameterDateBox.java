package com.codenvy.analytics.client.resources;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;
import java.util.Map;

public class ParameterDateBox extends DateBox {

    public ParameterDateBox(final Map<String, String> selectedParameters, final String parameterName, String dafaultValue) {
        super();

        setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("dd MMM yyyy")));
        setValue(DateTimeFormat.getFormat("yyyyMMdd").parse(dafaultValue));
        selectedParameters.put(parameterName, dafaultValue);

        this.addValueChangeHandler(new ValueChangeHandler<Date>() {
            public void onValueChange(ValueChangeEvent<Date> event) {
                selectedParameters.put(parameterName, DateTimeFormat.getFormat("yyyyMMdd").format(event.getValue()));
            }
        });
    }
}
