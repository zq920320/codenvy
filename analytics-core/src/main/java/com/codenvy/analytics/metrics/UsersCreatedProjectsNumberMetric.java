/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;
import com.codenvy.analytics.metrics.value.filters.UsersFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersCreatedProjectsNumberMetric extends CalculateBasedMetric {

    private final Metric createdUsersMetric;
    private Metric       createdProjectsMetric;

    UsersCreatedProjectsNumberMetric() throws IOException {
        super(MetricType.USERS_CREATED_PROJECTS_NUMBER);

        this.createdUsersMetric = MetricFactory.createMetric(MetricType.USERS_CREATED_LIST);
        this.createdProjectsMetric = MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST);
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return createdUsersMetric.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        int count = 0;

        ProjectsFilter pFilter = new ProjectsFilter((ListListStringValueData)createdProjectsMetric.getValue(context));
        Set<String> allUsersHaveCreatedProjects = pFilter.getAvailable(MetricFilter.FILTER_USER);
        
        UsersFilter uFilter = new UsersFilter((ListListStringValueData)createdUsersMetric.getValue(context));
        Set<String> usersCreated = uFilter.getAvailable(MetricFilter.FILTER_USER);

        for (String user : usersCreated) {
            if (allUsersHaveCreatedProjects.contains(user)) {
                count++;
            }
        }

        return new LongValueData(count);
    }
}
