/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.ProjectsJRebelFilter;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JRebelUsageListMetric extends ScriptBasedMetric {

    public static final String JREBEL_USAGE_TRUE = "true";

    JRebelUsageListMetric() {
        super(MetricType.JREBEL_USAGE_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.JREBEL_USAGE;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new ProjectsJRebelFilter((ListListStringValueData)valueData);
    }
}
