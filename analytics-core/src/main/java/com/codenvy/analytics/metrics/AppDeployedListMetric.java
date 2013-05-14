/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AppDeployedListMetric extends ScriptBasedMetric {

    AppDeployedListMetric() {
        super(MetricType.APP_DEPLOYED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.APP_DEPLOYED_LIST;
    }

    /** {@inheritDoc} */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new ProjectsFilter((ListListStringValueData)valueData);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }
}
