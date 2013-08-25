/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractTopSessionsMetric extends CalculatedMetric {

    public static final int LIFE_TIME_PERIOD = -1;
    private final int period;

    public AbstractTopSessionsMetric(MetricType metricType, int period) {
        super(metricType, MetricType.ACTIVE_FACTORY_SET);
        this.period = period;
    }

    /**
     * Context initialization accordingly to given period. For instance, if period is equal to 7, then
     * context will cover last 7 days.
     */
    private Map<String, String> initializeContext(int period) throws IOException {
        Map<String, String> context = Utils.newContext();
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

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }
}
