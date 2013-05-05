/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeOpenShiftNumberMetric extends ValueFromMapMetric {

    PaasDeploymentTypeOpenShiftNumberMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_OPENSHIFT_NUMBER, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES), ValueType.NUMBER,
              "OpenShift");
    }
}
