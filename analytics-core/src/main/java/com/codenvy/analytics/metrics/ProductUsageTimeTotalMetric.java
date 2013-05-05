/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeTotalMetric extends ScriptBasedMetric {

    ProductUsageTimeTotalMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOTAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PRODUCT_USAGE_TIME;
    }
}
