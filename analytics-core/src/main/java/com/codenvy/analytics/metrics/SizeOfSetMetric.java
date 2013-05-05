/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.SetValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class SizeOfSetMetric extends CalculateBasedMetric {

    private final Metric basedMetric;

    SizeOfSetMetric(MetricType metricType, Metric basedMetric) {
        super(metricType);
        this.basedMetric = basedMetric;
    }

    /** {@inheritedDoc} */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        SetValueData< ? > valueData = (SetValueData< ? >)basedMetric.getValue(context);
        return new LongValueData(valueData.getAll().size());
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /**
     * @return
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
