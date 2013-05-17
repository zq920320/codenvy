/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.ProductUsageTimeFilter;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeListlMetric extends ScriptBasedMetric {

    ProductUsageTimeListlMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PRODUCT_USAGE_TIME;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new ProductUsageTimeFilter((ListListStringValueData)valueData);
    }
}
