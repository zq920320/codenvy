/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectDeployedTypes extends NonAggregatedResultMetric {

    public ProjectDeployedTypes(String metricName) {
        super(metricName);
    }
    
    public ProjectDeployedTypes() {
        super(MetricType.PROJECT_DEPLOYED_TYPES);
    }

    @Override
    public String getDescription() {
        return "The number of deployed projects on specific PaaS";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
