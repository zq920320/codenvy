/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.MapStringFixedLongListValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeDomainsMetric extends ValueReadBasedMetric {

    public ProductUsageTimeDomainsMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_DOMAINS);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return MapStringFixedLongListValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE, MetricParameter.TO_DATE}));
    }

    @Override
    public String getDescription() {
        return "The list of product usage by domains, including the number of sessions and time usage in minutes";
    }
}
