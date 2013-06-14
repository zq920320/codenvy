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
public class ProductUsageTimeTopUsersBy90DayMetric extends ScriptBasedMetric {

    ProductUsageTimeTopUsersBy90DayMetric() {
        super(MetricType.PRODUCT_USAGE_TIME_TOP_USERS_BY_90DAY);
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
        context.put(MetricParameter.INTERVAL.name(), "P90D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());

        return super.getValue(context);
    }
}
