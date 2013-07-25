/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersCompletedProfile extends CalculateBasedMetric {

    private final UsersUpdateProfileList basedMetric;

    public UsersCompletedProfile() {
        super(MetricType.USERS_COMPLETED_PROFILE);
        basedMetric = (UsersUpdateProfileList)MetricFactory.createMetric(MetricType.USERS_UPDATE_PROFILE_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ValueData value = basedMetric.getValue(context);
        Filter filter = basedMetric.createFilter(value);

        return new LongValueData(filter.getAvailable(MetricFilter.FILTER_USER).size());
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }
}
