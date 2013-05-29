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
public class ProjectsDeployedLocalListMetric extends ScriptBasedMetric {

    ProjectsDeployedLocalListMetric() {
        super(MetricType.PROJECTS_DEPLOYED_LOCAL_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.PROJECTS_DEPLOYED_LOCAL;
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
