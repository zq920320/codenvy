/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.LongValueData;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestCumulativeMetric extends BaseTest {

    @Test
    public void testEvaluateValue() throws Exception {
        Map<String, String> contextCurrentDate = new HashMap<>();
        contextCurrentDate.put(Parameters.FROM_DATE.name(), "20120105");
        contextCurrentDate.put(Parameters.TO_DATE.name(), "20120105");
        contextCurrentDate.put(Parameters.TIME_UNIT.name(), Parameters.TimeUnit.DAY.toString());

        Metric mockedAddedMetric = mock(Metric.class);
        Metric mockedRemovedMetric = mock(Metric.class);

        doReturn(new LongValueData(10L)).when(mockedAddedMetric).getValue(anyMap());
        doReturn(new LongValueData(5L)).when(mockedRemovedMetric).getValue(anyMap());

        TestedMetric testedMetric = new TestedMetric(MetricType.TOTAL_WORKSPACES,
                                                     mockedAddedMetric,
                                                     mockedRemovedMetric);

        assertEquals(testedMetric.getValue(contextCurrentDate), new LongValueData(30L));
    }

    class TestedMetric extends CumulativeMetric {
        TestedMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) throws IOException {
            super(metricType, addedMetric, removedMetric);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
