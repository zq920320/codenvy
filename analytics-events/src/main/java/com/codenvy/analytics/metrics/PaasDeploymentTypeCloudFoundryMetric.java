/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeCloudFoundryMetric extends ValueFromMapMetric {

    PaasDeploymentTypeCloudFoundryMetric() throws IOException {
        super(MetricType.PERCENT_PAAS_DEPLOYEMNT_TYPE_CLOUDFOUNDRY, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "CloudFoundry",
              true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Cloud Foundry";
    }

}
