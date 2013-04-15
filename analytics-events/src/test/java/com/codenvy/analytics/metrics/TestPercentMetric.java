/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestPercentMetric {

    @Test
    public void testQueryValue() throws Exception {
        Metric baseMetric = mock(Metric.class);
        Metric relativeMetric = mock(Metric.class);

        doReturn(100L).when(baseMetric).getValue(anyMap());
        doReturn(5L).when(relativeMetric).getValue(anyMap());

        TestedMetric testedMetric =
                                    new TestedMetric(MetricType.PERCENT_ACTIVE_WORKSPACES, baseMetric,
                                                     relativeMetric, false);
        assertEquals(testedMetric.getValue(new HashMap<String, String>()), Double.valueOf(5.0D));

        testedMetric =
                       new TestedMetric(MetricType.PERCENT_INACTIVE_WORKSPACES, baseMetric,
                                        relativeMetric, true);
        assertEquals(testedMetric.getValue(new HashMap<String, String>()), Double.valueOf(95.0D));
    }

    class TestedMetric extends PercentMetric {

        TestedMetric(MetricType metricType, Metric baseMetric, Metric relativeMetric, boolean flag) throws IOException {
            super(metricType, baseMetric, relativeMetric, flag);
        }

        @Override
        public String getTitle() {
            return null;
        }
    }
}
