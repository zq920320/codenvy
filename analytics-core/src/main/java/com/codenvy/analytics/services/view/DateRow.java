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
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class DateRow extends AbstractRow {

    private static final String DAY_FORMAT_PARAM = "dayFormat";

    private static final String WEEK_FORMAT_PARAM = "weekFormat";

    private static final String MONTH_FORMAT_PARAM = "monthFormat";

    private static final String LIFE_TIME_FORMAT_PARAM = "lifeTimeFormat";

    private static final String DAY_FORMAT_DEFAULT = "MMM dd";

    private static final String WEEK_FORMAT_DEFAULT = "MMM dd";

    private static final String MONTH_FORMAT_DEFAULT = "MMM";

    private static final String LIFE_TIME_FORMAT_DEFAULT = "MMM dd";

    private final Map<Parameters.TimeUnit, String> format = new HashMap<>(4);

    public DateRow(Map<String, String> parameters) {
        super(parameters);
        format.put(Parameters.TimeUnit.DAY,
                   parameters.containsKey(DAY_FORMAT_PARAM) ? parameters.get(DAY_FORMAT_PARAM)
                                                            : DAY_FORMAT_DEFAULT);
        format.put(Parameters.TimeUnit.WEEK,
                   parameters.containsKey(WEEK_FORMAT_PARAM) ? parameters.get(WEEK_FORMAT_PARAM)
                                                             : WEEK_FORMAT_DEFAULT);
        format.put(Parameters.TimeUnit.MONTH,
                   parameters.containsKey(MONTH_FORMAT_PARAM) ? parameters.get(MONTH_FORMAT_PARAM)
                                                              : MONTH_FORMAT_DEFAULT);
        format.put(Parameters.TimeUnit.LIFETIME,
                   parameters.containsKey(LIFE_TIME_FORMAT_PARAM) ? parameters.get(LIFE_TIME_FORMAT_PARAM)
                                                                  : LIFE_TIME_FORMAT_DEFAULT);
    }

    private DateFormat getDateFormat() {
        return null;
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
            DateFormat dateFormat = new SimpleDateFormat(format.get(Utils.getTimeUnit(context)));

            Calendar toDate = Utils.getToDate(context);
            return new StringValueData(dateFormat.format(toDate.getTime()));
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }
}