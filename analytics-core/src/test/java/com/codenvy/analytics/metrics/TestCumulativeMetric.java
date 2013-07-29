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

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.BaseTest;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.codenvy.analytics.metrics.value.LongValueData;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestCumulativeMetric extends BaseTest {

    public void testEvaluateValue() throws Exception {
        Map<String, String> contextCurrentDate = new HashMap<String, String>();
        contextCurrentDate.put(MetricParameter.FROM_DATE.name(), "20130331");
        contextCurrentDate.put(MetricParameter.TO_DATE.name(), "20130331");
        contextCurrentDate.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.toString());
        
        Metric mockedAddedMetric = mock(Metric.class);
        Metric mockedRemovedMetric = mock(Metric.class);

        doReturn(new LongValueData(10L)).when(mockedAddedMetric).getValue(anyMap());
        doReturn(new LongValueData(5L)).when(mockedRemovedMetric).getValue(anyMap());

        TestedMetric testedMetric =
                                    spy(new TestedMetric(MetricType.TOTAL_WORKSPACES_NUMBER, mockedAddedMetric,
                                                         mockedRemovedMetric));

        doReturn(new LongValueData(100L)).when(testedMetric).getValue(anyMap());

        assertEquals(testedMetric.getValue(contextCurrentDate), new LongValueData(105L));
    }

    class TestedMetric extends CumulativeMetric {
        TestedMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) throws IOException {
            super(metricType, addedMetric, removedMetric);
        }

        @Override
        protected void validateExistenceInitialValueBefore(Map<String, String> context) throws InitialValueNotFoundException, IOException {
        }
    }
}
