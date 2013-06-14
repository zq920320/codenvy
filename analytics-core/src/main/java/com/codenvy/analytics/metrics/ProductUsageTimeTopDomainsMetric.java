/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeTopDomainsMetric extends AbstractMetric {

    ProductUsageTimeTopDomainsMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
