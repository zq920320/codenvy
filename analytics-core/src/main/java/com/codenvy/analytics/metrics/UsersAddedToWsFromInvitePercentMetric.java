/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersAddedToWsFromInvitePercentMetric extends CalculateBasedMetric {

    private final UsersAddedToWorkspaceListMetric basedMetric;

    UsersAddedToWsFromInvitePercentMetric() throws IOException {
        super(MetricType.USERS_ADDED_TO_WORKSPACE_FROM_INVITE_PERCENT);
        this.basedMetric = (UsersAddedToWorkspaceListMetric)MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)basedMetric.getValue(context);
        Filter filter = basedMetric.createFilter(valueData);
        
        int total = filter.size();
        int number = filter.size(MetricFilter.FILTER_USER_ADDED_FROM, UsersAddedToWorkspaceListMetric.INVITE);

        return total == 0 ? new DoubleValueData(Double.NaN) : new DoubleValueData(100D * number / total);
    }
}
