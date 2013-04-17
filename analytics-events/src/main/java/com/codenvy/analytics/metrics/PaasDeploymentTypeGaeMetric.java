/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeGaeMetric extends ValueFromMapMetric {

    PaasDeploymentTypeGaeMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYEMNT_TYPE_GAE, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "GAE",
              true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% GAE";
    }
}
