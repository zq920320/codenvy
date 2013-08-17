/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAbanMetric extends CalculatedMetric {

    private final Metric factorySessionsConv = MetricFactory.createMetric(MetricType.FACTORY_SESSIONS_CONV);

    public FactorySessionsAbanMetric() {
        super(MetricType.FACTORY_SESSIONS_ANON, MetricType.FACTORY_SESSIONS);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        LongValueData totalSessions = (LongValueData)super.getValue(context);
        LongValueData convertedSessions = (LongValueData)factorySessionsConv.getValue(context);

        return new LongValueData(totalSessions.getAsLong() - convertedSessions.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
