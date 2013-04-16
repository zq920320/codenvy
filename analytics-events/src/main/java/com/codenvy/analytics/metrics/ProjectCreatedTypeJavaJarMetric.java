/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaJarMetric extends ValueFromMapMetric {

    ProjectCreatedTypeJavaJarMetric() throws IOException {
        super(MetricType.PROJECT_TYPE_JAVA_JAR, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "Jar", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Java Jar";
    }
}
