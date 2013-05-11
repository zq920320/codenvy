/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
abstract public class ValueFromMapMetric extends CalculateBasedMetric {

    private final ValueType   valueType;
    private final Metric      basedMetric;
    private final Set<String> keys;
    private final Set<String> ignoredKeys;

    ValueFromMapMetric(MetricType metricType, Metric basedMetric, ValueType valueType, String key) {
        this(metricType, basedMetric, valueType, Arrays.asList(new String[]{key}), Collections.<String> emptySet());
    }

    ValueFromMapMetric(MetricType metricType,
                       Metric basedMetric,
                       ValueType valueType,
                       Collection<String> keys,
                       Collection<String> ignoredKeys) {
        super(metricType);

        this.valueType = valueType;
        this.keys = new HashSet<String>(keys);
        this.ignoredKeys = new HashSet<String>(ignoredKeys);
        this.basedMetric = basedMetric;
    }

    ValueFromMapMetric(MetricType metricType,
                       Metric basedMetric,
                       ValueType valueType,
                       String key,
                       String ignoredKey) {
        this(metricType, basedMetric, valueType, Arrays.asList(new String[]{key}), Arrays.asList(new String[]{ignoredKey}));
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritedDoc} */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        MapStringLongValueData basedMetricValue = (MapStringLongValueData)basedMetric.getValue(context);

        switch (valueType) {
            case NUMBER:
                return new DoubleValueData(calculateParticalValue(basedMetricValue));
            case PERCENT:
                return new DoubleValueData(calculatePercent(basedMetricValue));
            default:
                throw new IllegalStateException("Unknown type " + valueType);
        }
    }

    protected double calculatePercent(MapStringLongValueData basedMetricValue) {
        return 100D * calculateParticalValue(basedMetricValue) / calculateSum(basedMetricValue);
    }

    protected long calculateSum(MapStringLongValueData basedMetricValue) {
        long sum = 0L;
        Map<String, Long> all = basedMetricValue.getAll();

        for (Entry<String, Long> entry : all.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();

            if (!ignoredKeys.contains(key)) {
                sum += value;
            }
        }

        return sum;
    }

    protected double calculateParticalValue(MapStringLongValueData basedMetricValue) {
        long sum = 0;
        Map<String, Long> all = basedMetricValue.getAll();

        for (String key : keys) {
            if (all.containsKey(key)) {
                sum += all.get(key);
            }
        }

        return sum;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    enum ValueType {
        NUMBER,
        PERCENT
    }
}
