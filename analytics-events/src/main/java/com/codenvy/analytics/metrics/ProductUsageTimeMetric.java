/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeMetric extends ScriptBasedMetric {

    ProductUsageTimeMetric() {
        super(MetricType.PRODUCT_USAGE_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "Total Product Usage (mins)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PRODUCT_USAGE_TIME;
    }
}
