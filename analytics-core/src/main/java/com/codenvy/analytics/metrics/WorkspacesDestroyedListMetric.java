/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.WorkspacesFilter;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesDestroyedListMetric extends ScriptBasedMetric {

    WorkspacesDestroyedListMetric() {
        super(MetricType.WORKSPACES_DESTROYED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.WORKSPACES_DESTROYED;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new WorkspacesFilter((ListListStringValueData)valueData);
    }
}
