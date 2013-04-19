/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentProjectCreatedTypeGroovyMetric extends ValueFromMapMetric {

    PercentProjectCreatedTypeGroovyMetric() throws IOException {
        super(MetricType.PERCENT_PROJECT_TYPE_GROOVY, MetricFactory.createMetric(MetricType.PROJECT_CREATED_TYPES), "eXo",
              ValueType.BOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Groovy";
    }
}
