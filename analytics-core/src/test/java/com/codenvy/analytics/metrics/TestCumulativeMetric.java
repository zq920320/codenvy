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
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestCumulativeMetric extends BaseTest {

    @Test
    public void testEvaluateValue() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20120105");
        Parameters.TO_DATE.put(context, "20120105");
        Parameters.TIME_UNIT.put(context, "20120105");

        ReadBasedMetric mockedAddedMetric = mock(ReadBasedMetric.class);
        ReadBasedMetric mockedRemovedMetric = mock(ReadBasedMetric.class);

        doReturn(new LongValueData(40L)).when(mockedAddedMetric).getValue(anyMap());
        doReturn(new LongValueData(20L)).when(mockedRemovedMetric).getValue(anyMap());

        TestedMetric testedMetric = new TestedMetric(MetricType.TOTAL_WORKSPACES,
                                                     mockedAddedMetric,
                                                     mockedRemovedMetric);

        assertEquals(testedMetric.getValue(context), new LongValueData(30L));
    }

    @Test
    public void testReturnDefaultValueIfFilterExists() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user");
        Parameters.FROM_DATE.put(context, "20120105");
        Parameters.TO_DATE.put(context, "20120105");
        Parameters.TIME_UNIT.put(context, "20120105");

        ReadBasedMetric mockedAddedMetric = mock(ReadBasedMetric.class);
        ReadBasedMetric mockedRemovedMetric = mock(ReadBasedMetric.class);

        doReturn(new LongValueData(40L)).when(mockedAddedMetric).getValue(anyMap());
        doReturn(new LongValueData(20L)).when(mockedRemovedMetric).getValue(anyMap());

        TestedMetric testedMetric = new TestedMetric(MetricType.TOTAL_WORKSPACES,
                                                     mockedAddedMetric,
                                                     mockedRemovedMetric);

        assertEquals(testedMetric.getValue(context), LongValueData.DEFAULT);
    }

    class TestedMetric extends CumulativeMetric {
        TestedMetric(MetricType metricType, ReadBasedMetric addedMetric, ReadBasedMetric removedMetric)
                throws IOException {
            super(metricType, addedMetric, removedMetric);
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
