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
public class UsersDeployedLocalAndPaasProjectsNumberMetric extends CalculateBasedMetric {

    private final Metric basedMetric1;
    private final Metric basedMetric2;

    UsersDeployedLocalAndPaasProjectsNumberMetric() throws IOException {
        super(MetricType.USERS_DEPLOYED_LOCAL_AND_PAAS_PROJECTS_NUMBER);
        this.basedMetric1 = MetricFactory.createMetric(MetricType.PROJECTS_DEPLOYED_LOCAL_LIST);
        this.basedMetric2 = MetricFactory.createMetric(MetricType.PROJECTS_DEPLOYED_PAAS_LIST);
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric1.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData value = (ListListStringValueData)basedMetric1.getValue(context);
        ProjectsFilter filter = new ProjectsFilter(value);
        Set<String> users1 = filter.getAvailable(MetricFilter.FILTER_USER);

        value = (ListListStringValueData)basedMetric2.getValue(context);
        filter = new ProjectsFilter(value);
        Set<String> users2 = filter.getAvailable(MetricFilter.FILTER_USER);
        
        users1.retainAll(users2);

        return new LongValueData(users1.size());
    }
}
