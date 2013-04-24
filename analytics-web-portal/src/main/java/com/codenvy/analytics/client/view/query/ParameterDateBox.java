package com.codenvy.analytics.client.view.query;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;

class ParameterDateBox extends DateBox {
    private final QueryViewImpl  queryView;
    private final String         parameterName;
    private final DateTimeFormat showFormat  = DateTimeFormat.getFormat("dd MMM yyyy");
    private final DateTimeFormat storeFormat = DateTimeFormat.getFormat("yyyyMMdd");

    public ParameterDateBox(QueryViewImpl queryView, final String parameterName, String dafaultValue) {
        super();
        this.queryView = queryView;
        this.parameterName = parameterName;
        setFormat(new DateBox.DefaultFormat(showFormat));
        setValue(storeFormat.parse(dafaultValue));
        this.queryView.getSelectedMetricParameters().put(parameterName, dafaultValue);

        this.addValueChangeHandler(new ParameterDateBoxValueChangeHandler());
    }

    private class ParameterDateBoxValueChangeHandler implements ValueChangeHandler<Date> {

        public void onValueChange(ValueChangeEvent<Date> event) {
            queryView.getSelectedMetricParameters().put(parameterName, storeFormat.format(event.getValue()));
        }
    }
}
