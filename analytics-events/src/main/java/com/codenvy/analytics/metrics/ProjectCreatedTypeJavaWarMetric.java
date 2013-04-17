/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedTypeJavaWarMetric extends ValueFromMapMetric {

    ProjectCreatedTypeJavaWarMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_JAVA_WAR, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "War", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long getParticalValue(Map<String, Long> valueMetric) {
        Long value1 = valueMetric.containsKey(keyName) ? valueMetric.get(keyName) : Long.valueOf(0);
        Long value2 = valueMetric.containsKey("Java") ? valueMetric.get("Java") : Long.valueOf(0);

        return value1 + value2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Java War";
    }
}
