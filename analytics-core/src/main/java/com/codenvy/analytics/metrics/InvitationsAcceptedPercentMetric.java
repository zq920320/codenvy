/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class InvitationsAcceptedPercentMetric extends CalculateBasedMetric {

    private final Metric invitationSentMetric;
    private final Metric userAddedToWsMetric;

    InvitationsAcceptedPercentMetric() throws IOException {
        super(MetricType.INVITATIONS_ACCEPTED_PERCENT);

        this.invitationSentMetric = MetricFactory.createMetric(MetricType.INVITATIONS_SENT);
        this.userAddedToWsMetric = MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<MetricParameter> getParams() {
        Set<MetricParameter> params = invitationSentMetric.getParams();
        params.addAll(userAddedToWsMetric.getParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        MapStringLongValueData values = (MapStringLongValueData)userAddedToWsMetric.getValue(context);

        Map<StringValueData, LongValueData> all = values.getAll();
        StringValueData key = new StringValueData("invite");
        
        Long value = all.containsKey(key) ? all.get(key).getAsLong() : 0L;
        Long total = (Long)invitationSentMetric.getValue(context).getAsLong();

        return new DoubleValueData(100D * value / total);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }
}
