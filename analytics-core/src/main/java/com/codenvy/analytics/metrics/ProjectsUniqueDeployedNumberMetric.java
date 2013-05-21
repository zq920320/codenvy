/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsUniqueDeployedNumberMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    ProjectsUniqueDeployedNumberMetric() throws IOException {
        super(MetricType.PROJECTS_DEPLOYED_NUMBER);
        this.basedMetric = MetricFactory.createMetric(MetricType.PROJECTS_DEPLOYED_LIST);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)basedMetric.getValue(context);
        ProjectsFilter filter = new ProjectsFilter(valueData);

        return new LongValueData(filter.getUniqueProjects().size());
    }
}
