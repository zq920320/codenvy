/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.AppDeployedListFilter;
import com.codenvy.analytics.metrics.value.filters.ValueDataFilter;
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
    protected ValueDataFilter createFilter(ValueData valueData) {
        return new AppDeployedListFilter((ListListStringValueData)valueData);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }
}
