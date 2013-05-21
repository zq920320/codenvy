/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.SetListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;
import com.codenvy.analytics.scripts.ScriptType;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsSetMetric extends ScriptBasedMetric {

    ActiveProjectsSetMetric() {
        super(MetricType.ACTIVE_PROJECTS_SET);
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
        SetListStringValueData setVD = (SetListStringValueData)valueData;
        return new ProjectsFilter(new ListListStringValueData(setVD.getAll()));
    }
}
