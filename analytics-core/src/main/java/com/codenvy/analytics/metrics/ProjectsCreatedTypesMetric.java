/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectsCreatedTypesMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    ProjectsCreatedTypesMetric() throws IOException {
        super(MetricType.PROJECTS_CREATED_TYPES);
        this.basedMetric = MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST);
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return MapStringLongValueData.class;
    }

    /** {@inheritedDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)basedMetric.getValue(context);
        return new MapStringLongValueData(new ProjectsFilter(valueData).sizeOfGroups(MetricFilter.FILTER_PROJECT_TYPE));
    }
}
