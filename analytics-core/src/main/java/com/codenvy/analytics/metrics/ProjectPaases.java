/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectPaases extends AbstractMapValueResulted {

    public ProjectPaases() {
        super(MetricType.PROJECT_PAASES);
    }

    public ProjectPaases(String metricName) {
        super(metricName);
    }

    @Override
    public String getDescription() {
        return "The number of deployed projects on specific PaaS";
    }
}