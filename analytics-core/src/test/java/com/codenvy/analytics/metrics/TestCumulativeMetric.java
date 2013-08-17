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
import com.codenvy.analytics.metrics.value.ValueData;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestCumulativeMetric extends BaseTest {

    private final String content = "<metrics>" +
                                   "  <metric type=\"TOTAL_WORKSPACES\">" +
                                   "     <initial-value FROM_DATE=\"20091102\" TO_DATE=\"20091102\">1</initial-value>" +
                                   "     <initial-value FROM_DATE=\"20091103\" TO_DATE=\"20091103\">2</initial-value>" +
                                   "   <initial-value FROM_DATE=\"20130331\" TO_DATE=\"20130331\">100</initial-value>" +
                                   "  </metric>" +
                                   "  <metric type=\"TOTAL_USERS\">" +
                                   "    <initial-value FROM_DATE=\"20091104\" TO_DATE=\"20091104\">10</initial-value>" +
                                   "  </metric>" +
                                   "</metrics>";

    private InitialValueContainer initialValueContainer;

    @BeforeTest
    public void setUp() throws Exception {
        File file = new File(BASE_DIR, "initial-value.xml");
        file.createNewFile();

        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();

        System.setProperty(InitialValueContainer.ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY, file.getAbsolutePath());
        initialValueContainer = InitialValueContainer.getInstance();
    }

    @Test
    public void testEvaluateValue() throws Exception {
        Map<String, String> contextCurrentDate = new HashMap<>();
        contextCurrentDate.put(MetricParameter.FROM_DATE.name(), "20130402");
        contextCurrentDate.put(MetricParameter.TO_DATE.name(), "20130402");
        contextCurrentDate.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.toString());

        Metric mockedAddedMetric = mock(Metric.class);
        Metric mockedRemovedMetric = mock(Metric.class);

        doReturn(new LongValueData(10L)).when(mockedAddedMetric).getValue(anyMap());
        doReturn(new LongValueData(5L)).when(mockedRemovedMetric).getValue(anyMap());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Set<MetricParameter> parameters = new HashSet<>(3);
                parameters.add(MetricParameter.TO_DATE);
                parameters.add(MetricParameter.FROM_DATE);
                return parameters;
            }
        }).when(mockedAddedMetric).getParams();

        TestedMetric testedMetric =
                new TestedMetric(MetricType.TOTAL_WORKSPACES,
                                 mockedAddedMetric,
                                 mockedRemovedMetric);

        assertEquals(testedMetric.getValue(contextCurrentDate), new LongValueData(110L));

        Map<String, String> newContext = Utils.newContext();
        MetricParameter.TO_DATE.put(newContext, "20130401");
    }

    @Test
    public void testGetValues() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricParameter.TO_DATE.put(context, "20091102");

        ValueData valueData = initialValueContainer.getInitalValue(MetricType.TOTAL_WORKSPACES,
                                                                   context.toString());
        assertEquals(valueData, new LongValueData(1L));

        MetricParameter.TO_DATE.put(context, "20091103");
        valueData = initialValueContainer.getInitalValue(MetricType.TOTAL_WORKSPACES, context.toString());
        assertEquals(valueData, new LongValueData(2L));

        MetricParameter.TO_DATE.put(context, "20091104");
        valueData = initialValueContainer.getInitalValue(MetricType.TOTAL_USERS, context.toString());
        assertEquals(valueData, new LongValueData(10L));
    }

    @Test
    public void testValidation() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricParameter.TO_DATE.put(context, "20091104");


        initialValueContainer.validateExistenceInitialValueBefore(MetricType.TOTAL_WORKSPACES, context);
    }

    @Test(expectedExceptions = InitialValueNotFoundException.class)
    public void testValidationThrowExceptionCase1() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricParameter.TO_DATE.put(context, "20091101");

        initialValueContainer.validateExistenceInitialValueBefore(MetricType.TOTAL_USERS, context);
    }

    @Test(expectedExceptions = InitialValueNotFoundException.class)
    public void testValidationThrowExceptionCase2() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricParameter.TO_DATE.put(context, "20091101");


        initialValueContainer.validateExistenceInitialValueBefore(MetricType.TOTAL_WORKSPACES, context);
    }

    class TestedMetric extends CumulativeMetric {
        TestedMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) throws IOException {
            super(metricType, addedMetric, removedMetric);
        }
    }
}
