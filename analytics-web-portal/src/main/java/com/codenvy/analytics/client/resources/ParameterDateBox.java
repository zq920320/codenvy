/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

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
