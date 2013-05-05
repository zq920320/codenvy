/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Map;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.wrapper.DetailsProjectCreatedWrapper;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaWarPercentMetric extends ProjectsCreatedListMetric {

    ProjectCreatedTypeJavaWarPercentMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_JAVA_WAR_PERCENT, MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST), "War",
              ValueType.PERCENT);
    }

    /** {@inheritedDoc} */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        DetailsProjectCreatedWrapper wrapper = (DetailsProjectCreatedWrapper)getWrapper(basedMetric.getValue(context));
        return wrapper.getProjectsPercentByType(types[0]).union(wrapper.getProjectsPercentByType("Java"));
    }
}
