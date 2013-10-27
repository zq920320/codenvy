/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.LongValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FactorySessionsAndRunMetric extends ValueReadBasedMetric {

    public FactorySessionsAndRunMetric() {
        super(MetricType.FACTORY_SESSIONS_AND_RUN);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return super.getValue(alterFactoryFilter(context));
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    @Override
    public String getDescription() {
        return "The number of sessions where user run an application";
    }
}
