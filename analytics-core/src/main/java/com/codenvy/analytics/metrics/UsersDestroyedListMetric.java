/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.UsersFilter;
import com.codenvy.analytics.scripts.ScriptType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersDestroyedListMetric extends ScriptBasedMetric {

    UsersDestroyedListMetric() {
        super(MetricType.USERS_DESTROYED_LIST);
    }

    /** {@inheritedDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.USERS_REMOVED;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected Filter createFilter(ValueData valueData) {
        return new UsersFilter((ListListStringValueData)valueData);
    }
}
