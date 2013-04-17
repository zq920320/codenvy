/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypeAwsMetric extends ValueFromMapMetric {

    PaasDeploymentTypeAwsMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYEMNT_TYPE_AWS, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "AWS:BeansTalk", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% AWS";
    }
}
