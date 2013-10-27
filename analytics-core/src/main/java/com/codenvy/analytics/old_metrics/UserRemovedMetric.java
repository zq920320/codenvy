/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.LongValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserRemovedMetric extends ValueReadBasedMetric {

    UserRemovedMetric() {
        super(MetricType.USER_REMOVED);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    @Override
    public String getDescription() {
        return "The number of removed registered users";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
