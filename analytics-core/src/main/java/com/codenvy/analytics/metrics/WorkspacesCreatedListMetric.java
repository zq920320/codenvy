/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.UsersWorkspacesFilter;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class WorkspacesCreatedListMetric extends ScriptBasedMetric {

    WorkspacesCreatedListMetric() {
        super(MetricType.WORKSPACES_CREATED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.WORKSPACES_CREATED;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new UsersWorkspacesFilter((ListListStringValueData)valueData);
    }
}
