/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeLocalPercentMetric extends ValueFromMapMetric {

    PaasDeploymentTypeLocalPercentMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_LOCAL_PERCENT, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES), ValueType.PERCENT,
              "LOCAL");
    }
}
