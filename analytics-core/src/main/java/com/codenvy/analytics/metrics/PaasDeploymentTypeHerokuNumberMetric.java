/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeHerokuNumberMetric extends ValueFromMapMetric {

    PaasDeploymentTypeHerokuNumberMetric() {
        super(MetricType.PAAS_DEPLOYMENT_TYPE_HEROKU_NUMBER, MetricFactory.createMetric(MetricType.PAAS_DEPLOYMENT_TYPES), ValueType.NUMBER,
              "Heroku");
    }
}
