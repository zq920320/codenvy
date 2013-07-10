/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersShellLaunchedNumberMetric extends  CalculateBasedMetric {

    private final UsersShellLaunchedListMetric basedMetric;

    public UsersShellLaunchedNumberMetric() {
        super(MetricType.USERS_SHELL_LAUNCHED_NUMBER);
        basedMetric = (UsersShellLaunchedListMetric) MetricFactory.createMetric(MetricType.USERS_SHELL_LAUNCHED_LIST);
    }

    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        Filter filter = basedMetric.createFilter(basedMetric.getValue(context));
        return new LongValueData(filter.getAvailable(MetricFilter.FILTER_USER).size());
    }

    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }
}
