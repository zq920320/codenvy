/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.MetricParameter.ENTITY_TYPE;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProductUsageTimeTopDomainsByLifeTimeMetric extends ScriptBasedMetric {

    ProductUsageTimeTopDomainsByLifeTimeMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_1DAY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PRODUCT_USAGE_TIME_TOP;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context.put(MetricParameter.INTERVAL.getName(), "P100Y");
        context.put(MetricParameter.ENTITY.getName(), ENTITY_TYPE.DOMAINS.name());

        return super.getValue(context);
    }
}
