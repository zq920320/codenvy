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
public class ProductUsageTimeTotalMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    public ProductUsageTimeTotalMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOTAL);
        this.basedMetric = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_TIME_LIST);
    }

    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData value = (ListListStringValueData)basedMetric.getValue(context);

        ProductUsageTimeFilter filter = new ProductUsageTimeFilter(value);
        return new LongValueData(filter.getTotalUsageTime());
    }
}
