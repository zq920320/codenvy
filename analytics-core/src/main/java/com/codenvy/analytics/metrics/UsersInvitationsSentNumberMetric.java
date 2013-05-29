/*
 *    Copyright (C) 2013 Codenvy.
 *
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
 * Number of users, who created at least one project.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersInvitationsSentNumberMetric extends CalculateBasedMetric {

    private final InvitationsSentListMetric basedMetric;

    UsersInvitationsSentNumberMetric() throws IOException {
        super(MetricType.USERS_INVITATIONS_SENT_NUMBER);
        this.basedMetric = (InvitationsSentListMetric)MetricFactory.createMetric(MetricType.INVITATIONS_SENT_LIST);
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)basedMetric.getValue(context);
        Filter filter = basedMetric.createFilter(valueData);
        return new LongValueData(filter.getAvailable(MetricFilter.FILTER_USER).size());
    }
}
