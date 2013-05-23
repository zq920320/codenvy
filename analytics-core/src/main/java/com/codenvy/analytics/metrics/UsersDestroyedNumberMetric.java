/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersDestroyedNumberMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    UsersDestroyedNumberMetric() throws IOException {
        super(MetricType.USERS_DESTROYED_NUMBER);
        this.basedMetric = MetricFactory.createMetric(MetricType.USERS_DESTROYED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData value = (ListListStringValueData)basedMetric.getValue(context);
        return new LongValueData(value.size());
    }
}
