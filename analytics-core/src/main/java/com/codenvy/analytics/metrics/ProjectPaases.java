/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectPaases extends NonAggregatedResultMetric {

    public ProjectPaases(String metricName) {
        super(metricName);
    }

    public ProjectPaases() {
        super(MetricType.PROJECT_PAASES);
    }

    @Override
    public String getStorageTable() {
        return "project_paases";
    }

    @Override
    public String getDescription() {
        return "The number of deployed projects on specific PaaS";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return MapValueData.class;
    }
}
