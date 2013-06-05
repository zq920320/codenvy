/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeTopUsersByLifeTimeMetric extends ScriptBasedMetric {

    ProductUsageTimeTopUsersByLifeTimeMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_LIFETIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PRODUCT_USAGE_TIME_TOP_USERS;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = Utils.newContext();
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());
        context.put(MetricParameter.INTERVAL.getName(), "P100Y");

        return super.getValue(context);
    }
}
