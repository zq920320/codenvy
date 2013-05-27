/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeAwsPercentMetric extends ValueFromMapMetric {

    PaasDeploymentTypeAwsPercentMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_AWS_PERCENT, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES), ValueType.PERCENT,
              "AWS:BeansTalk", "Amazon");
    }
}
