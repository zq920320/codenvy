/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.MapStringListValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeUsersMetric extends ValueReadBasedMetric {

    public ProductUsageTimeUsersMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_USERS);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return MapStringListValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE, MetricParameter.TO_DATE}));
    }
}
