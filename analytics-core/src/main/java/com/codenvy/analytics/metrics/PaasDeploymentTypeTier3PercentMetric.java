/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeTier3PercentMetric extends ValueFromMapMetric {

    PaasDeploymentTypeTier3PercentMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_TIER3_PERCENT, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES),
              ValueType.PERCENT, "Tier3 Web Fabric", "Amazon");
    }
}
