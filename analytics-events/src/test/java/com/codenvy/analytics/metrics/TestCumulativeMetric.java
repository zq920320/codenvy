/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.scripts.ScriptParameters;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestCumulativeMetric {

    @Test
    public void testQueryValue() throws Exception {
        Map<String, String> contextCurrentDate = new HashMap<String, String>();
        contextCurrentDate.put(ScriptParameters.FROM_DATE.getName(), "20130331");
        contextCurrentDate.put(ScriptParameters.TO_DATE.getName(), "20130331");
        contextCurrentDate.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());
        
        Metric addedMetric = spy(MetricFactory.createMetric(MetricType.WORKSPACES_CREATED));
        Metric removedMetric = spy(MetricFactory.createMetric(MetricType.WORKSPACES_DESTROYED));

        doReturn(10L).when(addedMetric).getValue(anyMap());
        doReturn(5L).when(removedMetric).getValue(anyMap());

        TestedMetric testedMetric =
                                    spy(new TestedMetric(MetricType.TOTAL_WORKSPACES, addedMetric,
                                                         removedMetric));
        doReturn(100L).when(testedMetric).getValue(anyMap());

        assertEquals(testedMetric.evaluateValue(contextCurrentDate), Long.valueOf(105));
    }

    class TestedMetric extends CumulativeMetric {

        TestedMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) throws IOException {
            super(metricType, addedMetric, removedMetric);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        protected InputStream readResource() {
            return new ByteArrayInputStream("<metrics></metrics>".getBytes());
        }
    }
}
