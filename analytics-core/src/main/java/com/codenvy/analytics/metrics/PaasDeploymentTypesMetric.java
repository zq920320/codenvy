/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsDeployedFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class PaasDeploymentTypesMetric extends CalculateBasedMetric {

    private final Metric basedMetric;
    
    PaasDeploymentTypesMetric() throws IOException {
        super(MetricType.PAAS_DEPLOYMENT_TYPES);
        this.basedMetric = MetricFactory.createMetric(MetricType.PROJECTS_DEPLOYED_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return MapStringLongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData listVD = (ListListStringValueData)basedMetric.getValue(context);

        ProjectsDeployedFilter filter = new ProjectsDeployedFilter(listVD);
        filter = new ProjectsDeployedFilter(filter.getUniqueProjects());
        
        return filter.sizeOfGroups(MetricFilter.FILTER_PROJECT_PAAS);
    }
}
