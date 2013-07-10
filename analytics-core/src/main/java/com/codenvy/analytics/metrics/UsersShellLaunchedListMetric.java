/*
 * Copyright (C) 2013 Codenvy.
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
public class UsersShellLaunchedListMetric extends  PersistableScriptBasedMetric {

    public UsersShellLaunchedListMetric() {
        super(MetricType.USERS_SHELL_LAUNCHED_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.USERS_SHELL_LAUNCHED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new UsersWorkspacesFilter((ListListStringValueData) valueData);
    }
}
