/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.UsersSSOLoggedInFilter;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersSsoLoggedInListMetric extends PersistableScriptBasedMetric {

    UsersSsoLoggedInListMetric() {
        super(MetricType.USERS_SSO_LOGGED_IN_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.USERS_SSO_LOGGED_IN;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    @Override
    protected UsersSSOLoggedInFilter createFilter(ValueData valueData) {
        return new UsersSSOLoggedInFilter((ListListStringValueData)valueData);
    }
}
