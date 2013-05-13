/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeTier3NumberMetric extends ValueFromMapMetric {

    PaasDeploymentTypeTier3NumberMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_TIER3_NUMBER, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES),
              ValueType.NUMBER, "Tier3 Web Fabric");
    }
}
