/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;
import com.codenvy.analytics.metrics.value.filters.ProjectsFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
        Filter filter = getFilter(basedMetric.getValue(context));

        double total = 0;
        for (String type : types) {
            int size = filter.size(MetricFilter.FILTER_PROJECT_TYPE, type);

            switch (valueType) {
                case NUMBER:
                    total += size;
                    break;
                case PERCENT:
                    total += size * 100 / filter.size();
                    break;
                default:
                    throw new IllegalStateException("Unknown " + valueType);
            }
        }

        return new DoubleValueData(total);
    }

    protected Filter getFilter(ValueData valueData) {
        return new ProjectsFilter((ListListStringValueData)valueData);
    }

    /** {@inheritedDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }
}
