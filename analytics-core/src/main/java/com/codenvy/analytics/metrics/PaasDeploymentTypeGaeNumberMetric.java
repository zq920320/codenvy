/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeGaeNumberMetric extends ValueFromMapMetric {

    PaasDeploymentTypeGaeNumberMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_GAE_NUMBER, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES), ValueType.NUMBER,
              "GAE");
    }
}
