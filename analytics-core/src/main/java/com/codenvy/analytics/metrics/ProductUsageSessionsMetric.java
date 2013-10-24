/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageSessionsMetric extends ValueReadBasedMetric {

    public ProductUsageSessionsMetric() {
        super(MetricType.PRODUCT_USAGE_SESSIONS);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The list of users' sessions";
    }

    public String getUser(ListStringValueData valueData) {
        return valueData.getAll().get(1);
    }

    public long getTime(ListStringValueData valueData) {
        return Long.valueOf(valueData.getAll().get(3));
    }
}
