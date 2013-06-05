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
public class ProductUsageTimeTopDomainsBy60DayMetric extends ScriptBasedMetric {

    ProductUsageTimeTopDomainsBy60DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_60DAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PRODUCT_USAGE_TIME_TOP_DOMAINS;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = Utils.newContext();
        context.put(MetricParameter.TO_DATE.getName(), MetricParameter.TO_DATE.getDefaultValue());
        context.put(MetricParameter.INTERVAL.getName(), "P60D");

        return super.getValue(context);

    }
}
