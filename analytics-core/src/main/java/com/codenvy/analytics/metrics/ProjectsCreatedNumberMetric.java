/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsCreatedNumberMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    ProjectsCreatedNumberMetric() throws IOException {
        super(MetricType.PROJECTS_CREATED_NUMBER);
        this.basedMetric = MetricFactory.createMetric(MetricType.PROJECTS_CREATED_LIST);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)basedMetric.getValue(context);
        return new LongValueData(valueData.size());
    }
}
