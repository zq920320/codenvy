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


package com.codenvy.analytics.services.view;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class DateRow extends AbstractRow {

    private static final String FORMAT = "format";

    private final DateFormat dateFormat;

    public DateRow(Map<String, String> parameters) {
        super(parameters);
        dateFormat = new SimpleDateFormat(parameters.get(FORMAT));
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getDescription() throws IOException {
        return new StringValueData("Date");
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getData(Map<String, String> context) throws IOException {
        try {
            Calendar toDate = Utils.getToDate(context);
            return new StringValueData(dateFormat.format(toDate.getTime()));
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}