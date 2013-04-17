/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentProjectCreatedTypeJavaSpring extends ValueFromMapMetric {


    PercentProjectCreatedTypeJavaSpring() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_JAVA_SPRING, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "Spring", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Java Spring";
    }
}
