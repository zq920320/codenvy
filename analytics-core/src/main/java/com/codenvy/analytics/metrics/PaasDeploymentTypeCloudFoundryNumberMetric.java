/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeCloudFoundryNumberMetric extends ValueFromMapMetric {

    PaasDeploymentTypeCloudFoundryNumberMetric() {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_NUMBER, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES),
              ValueType.NUMBER, "CloudFoundry");
    }
}
