/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentPaasDeploymentTypeLocalMetric extends ValueFromMapMetric {

    PercentPaasDeploymentTypeLocalMetric() throws IOException {
        super(MetricType.PERCENT_PAAS_DEPLOYEMNT_TYPE_LOCAL, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "LOCAL",
              ValueType.BOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Local";
    }
}
