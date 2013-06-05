/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeAwsNumberMetric extends ValueFromMapMetric {

    PaasDeploymentTypeAwsNumberMetric() {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_AWS_NUMBER, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES), ValueType.NUMBER,
              "AWS:BeansTalk");
    }
}
