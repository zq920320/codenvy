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
import com.codenvy.analytics.datamodel.LongValueData;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for initial values of some metrics. It is used in case when statistics had been started collecting not
 * from very beginning. The configuration has to be placed in analytics.properties and defines the date and initial
 * values of metrics. The example belows explains that statistic had been started collecting from 2012-01-01 but the
 * day before (on 2011-12-31) 10 workspaces, 20 users and 30 projects already had been created.
 * <p/>
 * analytics.initial_values.date=2011-12-31
 * analytics.initial_values.metrics=total_workspaces,total_users,total_projects
 * analytics.initial_values.metric_total_workspaces=10
 * analytics.initial_values.metric_total_users=20
 * analytics.initial_values.metric_total_projects=30
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@Singleton
public class InitialValueContainer {

    private static final String INITIAL_VALUES_DATE    = "analytics.initial_values.date";
    private static final String INITIAL_VALUES_METRICS = "analytics.initial_values.metrics";
    private static final String INITIAL_VALUES_METRIC  = "analytics.initial_values.metric_";

    private final Calendar                   initialValueDate = Calendar.getInstance();
    private final Map<String, LongValueData> initialValues    = new HashMap<>();

    @Inject
    public InitialValueContainer(Configurator configurator) {
        for (String name : configurator.getArray(INITIAL_VALUES_METRICS)) {
            String key = INITIAL_VALUES_METRIC + name;
            LongValueData initialValue = new LongValueData(configurator.getInt(key));

            initialValues.put(name.toLowerCase(), initialValue);
        }

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(configurator.getString(INITIAL_VALUES_DATE));
            initialValueDate.setTime(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException();
        }
    }

    /** @return the date for initial values */
    public Calendar getInitialValueDate() {
        return initialValueDate;
    }

    /** @return initial value for give metric or null */
    public LongValueData getInitialValue(String metricName) throws ParseException {
        return initialValues.get(metricName.toLowerCase());
    }

    /**
     * Checks if container contains initial value for given metric below or equal to the given date.
     *
     * @param context
     */
    public void validateExistenceInitialValueBefore(Context context)
            throws InitialValueNotFoundException {

        try {
            Calendar toDate = context.getAsDate(Parameters.TO_DATE);

            if (toDate.before(initialValueDate)) {
                throw new InitialValueNotFoundException("There is no initial value below given date");
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
