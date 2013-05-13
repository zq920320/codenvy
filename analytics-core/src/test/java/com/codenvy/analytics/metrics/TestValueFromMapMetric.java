/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;
import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestValueFromMapMetric {

    @Test
    public void testGetValue() throws Exception {
        Map<String, Long> all = new HashMap<String, Long>();
        all.put("key1", 10L);
        all.put("key2", 20L);
        all.put("key3", 20L);
        ValueData valueData = new MapStringLongValueData(all);

        Metric mockedMetric = mock(Metric.class);
        doReturn(valueData).when(mockedMetric).getValue(anyMap());

        DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);

        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.getName(), df.format(new Date()));
        context.put(MetricParameter.TO_DATE.getName(), df.format(new Date()));
        context.put(MetricParameter.TIME_UNIT.getName(), TimeUnit.DAY.toString());

        TestedMetric testedMetric = new TestedMetric(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, mockedMetric, ValueType.NUMBER, "key1");

        DoubleValueData result = (DoubleValueData)testedMetric.getValue(context);
        assertEquals(result, new DoubleValueData(10));

        testedMetric = new TestedMetric(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, mockedMetric, ValueType.PERCENT, "key1");

        result = (DoubleValueData)testedMetric.getValue(context);
        assertEquals(result, new DoubleValueData(20));
    }

    class TestedMetric extends ValueFromMapMetric {
        TestedMetric(MetricType metricType, Metric basedMetric, ValueType valueType, String key) {
            super(metricType, basedMetric, valueType, key);
        }
    }
}
