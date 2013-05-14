/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;

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

        this.createdUsersMetric = MetricFactory.createMetric(MetricType.USERS_CREATED_SET);
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

        ListListStringValueData createdProjectsValue = (ListListStringValueData)createdProjectsMetric.getValue(context);
        ProjectsFilter filter = new ProjectsFilter(createdProjectsValue);

        Set<String> allUsersHaveCreatedProjects = filter.getAvailable(MetricFilter.FILTER_USER).getAll();

        SetStringValueData createdUsersValue = (SetStringValueData)createdUsersMetric.getValue(context);
        Set<String> usersCreated = createdUsersValue.getAll();
        for (String user : usersCreated) {
            if (allUsersHaveCreatedProjects.contains(user)) {
                count++;
            }
        }

        return new LongValueData(count);
    }
}
