/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProductUsageTimeFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTime60_MoreMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    public ProductUsageTime60_MoreMetric() throws IOException {
        super(MetricType.PRODUCT_USAGE_TIME_60_MORE);
        this.basedMetric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_TIME_LIST);
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

        ProductUsageTimeFilter filter = new ProductUsageTimeFilter(value);
        return new LongValueData(filter.getUsageTime(60 * 60, false, Integer.MAX_VALUE, false));
    }
}
