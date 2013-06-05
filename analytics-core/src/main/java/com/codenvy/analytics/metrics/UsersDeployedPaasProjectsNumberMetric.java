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
 * Number of users, who built at least one project.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersDeployedPaasProjectsNumberMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    UsersDeployedPaasProjectsNumberMetric() {
        super(MetricType.USERS_DEPLOYED_PAAS_PROJECTS_NUMBER);
        this.basedMetric = MetricFactory.createMetric(MetricType.PROJECTS_DEPLOYED_PAAS_LIST);
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ProjectsFilter filter = new ProjectsFilter((ListListStringValueData)basedMetric.getValue(context));
        return new LongValueData(filter.getAvailable(MetricFilter.FILTER_USER).size());
    }
}
