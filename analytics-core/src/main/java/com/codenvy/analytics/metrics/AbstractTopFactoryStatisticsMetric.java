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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractTopFactoryStatisticsMetric extends CalculatedMetric {

    public static final int LIFE_TIME_PERIOD = -1;

    private final int period;

    AbstractTopFactoryStatisticsMetric(MetricType metricType, MetricType basedMetric, int period) {
        super(metricType, basedMetric);
        this.period = period;
    }

    /**
     * Set date period accordingly to given period. For instance, if period is equal to 7, then
     * context will cover last 7 days.
     */
    protected Map<String, String> getContextWithDatePeriod(Map<String, String> context) throws ParseException {
        context = Utils.clone(context);
        MetricParameter.TO_DATE.putDefaultValue(context);

        if (period == LIFE_TIME_PERIOD) {
            MetricParameter.FROM_DATE.putDefaultValue(context);
        } else {
            Calendar date = Utils.getToDate(context);
            date.add(Calendar.DAY_OF_MONTH, 1 - period);

            Utils.putFromDate(context, date);
        }

        return context;
    }
}
