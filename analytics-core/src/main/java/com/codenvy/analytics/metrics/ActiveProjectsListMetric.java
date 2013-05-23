/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;
import com.codenvy.analytics.scripts.ScriptType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsListMetric extends ScriptBasedMetric {

    ActiveProjectsListMetric() {
        super(MetricType.ACTIVE_PROJECTS_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.ACTIVE_PROJECTS;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isFilterSupported() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected ProjectsFilter createFilter(ValueData valueData) {
        return new ProjectsFilter((ListListStringValueData)valueData);
    }
}
