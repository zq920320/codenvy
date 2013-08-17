/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageTimeFactoryTotalMetric extends CalculatedMetric {
    public ProductUsageTimeFactoryTotalMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_FACTORY_TOTAL, MetricType.PRODUCT_USAGE_TIME_FACTORY);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)super.getValue(context);

        long total = 0;
        for (ListStringValueData item : valueData.getAll()) {
            total += Long.valueOf(item.getAll().get(3));
        }

        return new LongValueData(total / 60);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
