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
public class JRebelUsagePercentMetric extends CalculateBasedMetric {

    private final JRebelUsageListMetric basedMetric;

    JRebelUsagePercentMetric() throws IOException {
        super(MetricType.JREBEL_USAGE_PERCENT);
        this.basedMetric = (JRebelUsageListMetric)MetricFactory.createMetric(MetricType.JREBEL_USAGE_LIST);
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
        ListListStringValueData value = (ListListStringValueData)basedMetric.getValue(context);
        Filter filter = basedMetric.createFilter(value);
        
        Map<String, Long> sizeOfGroups = filter.sizeOfGroups(MetricFilter.FILTER_PROJECT_JREBEL_USAGE);

        int total = filter.size();
        Long usage = sizeOfGroups.get(JRebelUsageListMetric.JREBEL_USAGE_TRUE);

        return usage == null ? new DoubleValueData(0) : (total == 0 ? new DoubleValueData(Double.NaN) : new DoubleValueData(100D * usage
                                                                                                                            / total));

    }
}
