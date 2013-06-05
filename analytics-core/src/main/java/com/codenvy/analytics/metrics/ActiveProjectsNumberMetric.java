/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveProjectsNumberMetric extends CalculateBasedMetric {

    private final ActiveProjectsListMetric basedMetric;

    ActiveProjectsNumberMetric() {
        super(MetricType.ACTIVE_PROJECTS_NUMBER);
        this.basedMetric = (ActiveProjectsListMetric)MetricFactory.createMetric(MetricType.ACTIVE_PROJECTS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ProjectsFilter filter = basedMetric.createFilter(basedMetric.getValue(context));
        return new LongValueData(filter.getUniqueProjects().size());
    }
}
