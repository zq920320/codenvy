/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsConvMetric extends CalculatedMetric {

    public FactorySessionsConvMetric() {
        super(MetricType.FACTORY_SESSIONS_CONV, MetricType.FACTORY_PROJECT_IMPORTED);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of converted sessions in temporary workspaces";
    }
}
