/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.metrics.ValueFromMapMetric.ValueType;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestProjectsCreatedMetric {

    private ListStringValueData item1;
    private ListStringValueData item2;
    private ListStringValueData item3;
    private ListStringValueData item4;
    private Metric basedMetric;

    @BeforeMethod
    public void setUp() throws Exception {
        item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1"));
        item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "project1", "type2"));
        item3 = new ListStringValueData(Arrays.asList("ws2", "user2", "project2", "type3"));
        item4 = new ListStringValueData(Arrays.asList("ws2", "user3", "project3", "type2"));
        ListListStringValueData valueData = new ListListStringValueData(Arrays.asList(item1, item2, item3, item4));

        basedMetric = mock(Metric.class);
        doReturn(valueData).when(basedMetric).getValue(anyMap());
    }

    @Test
    public void testEvaluateNaN() throws Exception {
        basedMetric = mock(Metric.class);
        doReturn(new ListListStringValueData(new ArrayList<ListStringValueData>())).when(basedMetric).getValue(anyMap());

        TestedProjectsCreatedMetric metric = spy(new TestedProjectsCreatedMetric(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, basedMetric,
                                                                                 "type2", ValueType.PERCENT));
        ValueData valueData = metric.evaluate(Utils.newContext());
        assertEquals(valueData.getAsDouble(), Double.NaN);
    }
    
    @Test
    public void testEvaluateNumber() throws Exception {
        TestedProjectsCreatedMetric metric = spy(new TestedProjectsCreatedMetric(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, basedMetric,
                                                                                 "type2", ValueType.NUMBER));
        ValueData valueData = metric.evaluate(Utils.newContext());
        assertEquals(valueData.getAsDouble(), Double.valueOf(2));
    }

    @Test
    public void testEvaluatePercent() throws Exception {
        TestedProjectsCreatedMetric metric = spy(new TestedProjectsCreatedMetric(MetricType.PROJECT_TYPE_JAVA_JAR_NUMBER, basedMetric,
                                                                                 "type2", ValueType.PERCENT));
        ValueData valueData = metric.evaluate(Utils.newContext());
        assertEquals(valueData.getAsDouble(), Double.valueOf(50));
    }

    class TestedProjectsCreatedMetric extends AbstractProjectsCreatedMetric {
        TestedProjectsCreatedMetric(MetricType metricType, Metric basedMetric, String type, ValueType valueType) {
            super(metricType, basedMetric, type, valueType);
        }
    }
}
