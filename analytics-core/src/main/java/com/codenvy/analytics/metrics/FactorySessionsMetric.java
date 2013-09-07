/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsMetric extends ListMetric {

    public FactorySessionsMetric() {
        super(MetricType.FACTORY_SESSIONS, new MetricType[]{MetricType.FACTORY_SESSIONS_ANON,
                                                            MetricType.FACTORY_SESSIONS_AUTH});
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The total number of sessions in temporary workspaces";
    }
}
