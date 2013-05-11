/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.LongValueData;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestPercentMetric {

    @Test
    public void testGetValue() throws Exception {
        DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);

        Metric baseMetric = mock(Metric.class);
        Metric relativeMetric = mock(Metric.class);

        doReturn(new LongValueData(100L)).when(baseMetric).getValue(anyMap());
        doReturn(new LongValueData(5L)).when(relativeMetric).getValue(anyMap());

        Map<String, String> context = new HashMap<String, String>();
        context.put(MetricParameter.FROM_DATE.getName(), df.format(new Date()));
        context.put(MetricParameter.TO_DATE.getName(), df.format(new Date()));
        context.put(MetricParameter.TIME_UNIT.getName(), TimeUnit.DAY.toString());

        TestedMetric testedMetric =
                                    new TestedMetric(MetricType.ACTIVE_WORKSPACES_PERCENT, baseMetric,
                                                     relativeMetric, false);
        assertEquals(testedMetric.getValue(context), new DoubleValueData(5.0D));

        testedMetric =
                       new TestedMetric(MetricType.ACTIVE_WORKSPACES_PERCENT, baseMetric,
                                        relativeMetric, true);
        assertEquals(testedMetric.getValue(context), new DoubleValueData(95.0D));
    }

    class TestedMetric extends PercentMetric {

        TestedMetric(MetricType metricType, Metric baseMetric, Metric relativeMetric, boolean flag) throws IOException {
            super(metricType, baseMetric, relativeMetric, flag);
        }
    }
}
