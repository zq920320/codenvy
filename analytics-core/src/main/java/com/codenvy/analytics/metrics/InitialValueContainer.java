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

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for initial values of some metrics. It is used in case when statistics had been started collecting not
 * from very beginning. The configuration has to be placed in analytics.conf and defines the date and initial values of
 * metrics. The example belows explains that statistic had been started collecting from 2012-01-01 but the day before
 * (on 2011-12-31) 10 workspaces, 20 users and 30 projects already had been created.
 * <p/>
 * initial.value.date=2011-12-31
 * initial.value.metrics=total_workspaces,total_users,total_projects
 * initial.value.metric.total_workspaces=10
 * initial.value.metric.total_users=20
 * initial.value.metric.total_projects=30
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class InitialValueContainer {

    private static final String                 INITIAL_VALUE_METRICS = "initial.value.metrics";
    private static final String                 INITIAL_VALUE_METRIC  = "initial.value.metric.";
    private static final String                 INITIAL_VALUE_DATE    = "initial.value.date";
    private static final Calendar               initialValueDate      = Calendar.getInstance();
    private static final Map<String, ValueData> initialValues         = new HashMap<>();

    static {
        for (String name : Configurator.getArray(INITIAL_VALUE_METRICS)) {
            String key = INITIAL_VALUE_METRIC + name;
            LongValueData initialValue = new LongValueData(Configurator.getInt(key));

            initialValues.put(name.toLowerCase(), initialValue);
        }

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(Configurator.getString(INITIAL_VALUE_DATE));
            initialValueDate.setTime(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException();
        }
    }

    /** @return the date for initial values */
    public static Calendar getInitialValueDate() {
        return initialValueDate;
    }

    /** @return initial value for give metric or null */
    public static ValueData getInitialValue(String metricName) throws ParseException {
        return initialValues.get(metricName.toLowerCase());
    }

    /** Checks if container contains initial value for given metric below or equal to the given date. */
    public static void validateExistenceInitialValueBefore(Map<String, String> context)
            throws InitialValueNotFoundException {

        try {
            Calendar toDate = Utils.getToDate(context);

            if (toDate.before(initialValueDate)) {
                throw new InitialValueNotFoundException("There is no initial value below given date");
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
