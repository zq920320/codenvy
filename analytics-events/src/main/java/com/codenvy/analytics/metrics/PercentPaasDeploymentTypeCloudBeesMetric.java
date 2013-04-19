/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentPaasDeploymentTypeCloudBeesMetric extends ValueFromMapMetric {

    PercentPaasDeploymentTypeCloudBeesMetric() throws IOException {
        super(MetricType.PERCENT_PAAS_DEPLOYEMNT_TYPE_CLOUDBESS, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "CloudBees",
              ValueType.PERCENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Cloudbees";
    }
}
