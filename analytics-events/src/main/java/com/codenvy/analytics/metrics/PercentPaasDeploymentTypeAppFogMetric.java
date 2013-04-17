/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentPaasDeploymentTypeAppFogMetric extends ValueFromMapMetric {

    PercentPaasDeploymentTypeAppFogMetric() throws IOException {
        super(MetricType.PERCENT_PAAS_DEPLOYEMNT_TYPE_APPFOG, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "Appfog", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% AppFog";
    }
}
