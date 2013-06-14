/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestInitialValueContainer extends BaseTest {

    private final String          content = "<metrics>" +
                                            "  <metric type=\"TOTAL_WORKSPACES_NUMBER\">" +
                                            "     <initial-value fromDate=\"20091102\" toDate=\"20091102\">1</initial-value>" +
                                            "     <initial-value fromDate=\"20091103\" toDate=\"20091103\">2</initial-value>" +
                                            "  </metric>" +
                                            "  <metric type=\"TOTAL_USERS_NUMBER\">" +
                                            "     <initial-value fromDate=\"20091104\" toDate=\"20091104\">10</initial-value>" +
                                            "  </metric>" +
                                            "</metrics>";

    private InitialValueContainer mockedContainer;

    private Map<String, String>   context1;
    private Map<String, String>   context2;
    private Map<String, String>   context3;
    private Map<String, String>   context4;
    private Map<String, String>   context5;

    @BeforeMethod
    public void setUp() throws Exception {
        context1 = new LinkedHashMap<String, String>();
        context1.put("FROM_DATE", "20091102");
        context1.put("TO_DATE", "20091102");

        context2 = new LinkedHashMap<String, String>();
        context2.put("FROM_DATE", "20091103");
        context2.put("TO_DATE", "20091103");

        context3 = new LinkedHashMap<String, String>();
        context3.put("FROM_DATE", "20091103");
        context3.put("TO_DATE", "20091103");

        context4 = new LinkedHashMap<String, String>();
        context4.put("FROM_DATE", "20091104");
        context4.put("TO_DATE", "20091104");

        context5 = new LinkedHashMap<String, String>();
        context5.put("FROM_DATE", "20091101");
        context5.put("TO_DATE", "20091101");


        mockedContainer = spy(InitialValueContainer.getInstance());
        AbstractMetric mockedMetric = mock(AbstractMetric.class);

        doReturn(new ByteArrayInputStream(content.getBytes())).when(mockedContainer).readResource();
        doReturn(mockedMetric).when(mockedContainer).prepareMetric(any(MetricType.class));
        
        doReturn(LongValueData.class).when(mockedMetric).getValueDataClass();
        doReturn(context1).doReturn(context2).doReturn(context3).when(mockedMetric).makeUUID(anyMap());
    }

    @Test
    public void testGetValues() throws Exception {
        ValueData valueData = mockedContainer.getInitalValue(MetricType.TOTAL_WORKSPACES_NUMBER, context1.toString());
        assertEquals(valueData, new LongValueData(1L));

        valueData = mockedContainer.getInitalValue(MetricType.TOTAL_WORKSPACES_NUMBER, context2.toString());
        assertEquals(valueData, new LongValueData(2L));

        valueData = mockedContainer.getInitalValue(MetricType.TOTAL_USERS_NUMBER, context3.toString());
        assertEquals(valueData, new LongValueData(10L));
    }

    @Test
    public void testValidation() throws Exception {
        mockedContainer.validateExistenceInitialValueBefore(MetricType.TOTAL_WORKSPACES_NUMBER, context1);
    }
    
    @Test(expectedExceptions = InitialValueNotFoundException.class)
    public void testValidationThrowExceptionCase1() throws Exception {
        mockedContainer.validateExistenceInitialValueBefore(MetricType.TOTAL_USERS_NUMBER, context5);
    }

    @Test(expectedExceptions = InitialValueNotFoundException.class)
    public void testValidationThrowExceptionCase2() throws Exception {
        mockedContainer.validateExistenceInitialValueBefore(MetricType.TOTAL_WORKSPACES_NUMBER, context5);
    }
}
