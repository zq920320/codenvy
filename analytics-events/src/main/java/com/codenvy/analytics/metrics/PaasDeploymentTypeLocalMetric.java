/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeLocalMetric extends ValueFromMapMetric {

    PaasDeploymentTypeLocalMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYEMNT_TYPE_LOCAL, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "LOCAL",
              true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Local";
    }
}
