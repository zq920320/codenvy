/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PercentPaasDeploymentTypeHerokuMetric extends ValueFromMapMetric {

    PercentPaasDeploymentTypeHerokuMetric() throws IOException {
        super(MetricType.PERCENT_PAAS_DEPLOYEMNT_TYPE_HEROKU, MetricFactory.createMetric(MetricType.PAAS_DEPLOYEMNT_TYPES), "Heroku",
              ValueType.PERCENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "% Heroku";
    }
}
