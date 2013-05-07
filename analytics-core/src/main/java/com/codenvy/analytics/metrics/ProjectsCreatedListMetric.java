/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.filters.ProjectCreatedListFilter;
import com.codenvy.analytics.metrics.value.filters.ValueDataFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ProjectsCreatedListMetric extends CalculateBasedMetric {

    protected final ValueType valueType;
    protected final String[]  types;
    protected final Metric    basedMetric;

    ProjectsCreatedListMetric(MetricType metricType, Metric basedMetric, String type, ValueType valueType) {
        super(metricType);

        this.valueType = valueType;
        this.types = new String[]{type};
        this.basedMetric = basedMetric;
    }

    ProjectsCreatedListMetric(MetricType metricType, Metric basedMetric, String[] types, ValueType valueType) {
        super(metricType);

        this.valueType = valueType;
        this.types = types;
        this.basedMetric = basedMetric;
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        ProjectCreatedListFilter wrapper = (ProjectCreatedListFilter)getWrapper(basedMetric.getValue(context));

        ValueData total = null;
        for (String type : types) {
            switch (valueType) {
                case NUMBER:
                    total = total == null ? wrapper.getProjectsNumberByType(type) : total.union(wrapper.getProjectsNumberByType(type));
                    break;
                case PERCENT:
                    total = total == null ? wrapper.getProjectsPercentByType(type) : total.union(wrapper.getProjectsPercentByType(type));
                    break;
                default:
                    throw new IllegalStateException("Unknown " + valueType);
            }
        }

        return total;
    }

    protected ValueDataFilter getWrapper(ValueData valueData) {
        return new ProjectCreatedListFilter(
                                                (ListListStringValueData)valueData);
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }
}
