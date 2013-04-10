/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codenvy.analytics.scripts.ScriptParameters;

import org.testng.Assert;
import org.testng.annotations.Test;

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
        
        MetricType addedTypeMocked = mock(MetricType.class);
        MetricType removedTypeMocked = mock(MetricType.class);
        Metric addedMetricMocked = mock(Metric.class);
        Metric removedMetricMocked = mock(Metric.class);
        
        when(addedTypeMocked.getInstance()).thenReturn(addedMetricMocked);
        when(removedTypeMocked.getInstance()).thenReturn(removedMetricMocked);
        when(addedMetricMocked.getValue(contextCurrentDate)).thenReturn(10L);
        when(removedMetricMocked.getValue(contextCurrentDate)).thenReturn(5L);
        
        MetricType prevTypeMocked = mock(MetricType.class);
        CumulativeCalculatedMetric prevMetricMocked = mock(CumulativeCalculatedMetric.class);

        when(prevTypeMocked.getInstance()).thenReturn(prevMetricMocked);
        when(prevMetricMocked.getValue(anyMap())).thenReturn(100L);

        TestedMetric metric = new TestedMetric(prevTypeMocked, addedTypeMocked, removedTypeMocked);
        Assert.assertEquals(metric.queryValue(contextCurrentDate), Long.valueOf(105));
    }

    class TestedMetric extends CumulativeCalculatedMetric {

        TestedMetric(MetricType metricType, MetricType addedType, MetricType removedType) {
            super(metricType, addedType, removedType);
        }

        public TestedMetric() {
            super(null, null, null);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        protected ValueManager getValueManager() {
            return new LongValueManager();
        }
    }
}
