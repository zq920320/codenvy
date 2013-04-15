/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ScriptType;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestAbstractMetrics {

    private ScriptExecutor      mockedScriptExecutor;
    private AbstractMetric      metric;
    private Map<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        mockedScriptExecutor = mock(ScriptExecutor.class);
        metric = new TestedMetric(mockedScriptExecutor, MetricType.WORKSPACES_CREATED);
        context = new HashMap<String, String>();
    }

    /**
     * Calculated value should not be stored since {@link ScriptParameters#TO_DATE} is current date.
     */
    @Test(expectedExceptions = FileNotFoundException.class)
    public void shouldNotStoreValue() throws Exception {
        when(mockedScriptExecutor.executeAndReturnResult()).thenReturn(100L);

        context.put(ScriptParameters.FROM_DATE.getName(), "20130301");
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(Calendar.getInstance().getTime()));

        assertEquals(metric.getValue(context), 100L);

        // expects throwing exception, since file is absent
        metric.loadValue(context);
    }

    /**
     * Calculated value will be stored since {@link ScriptParameters#TO_DATE} is less than current date.
     */
    @Test
    public void shouldStoreValue() throws Exception {
        Calendar prevDate = Calendar.getInstance();
        prevDate.add(Calendar.DAY_OF_MONTH, -1);

        context.put(ScriptParameters.FROM_DATE.getName(), "20130301");
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.PARAM_DATE_FORMAT.format(prevDate.getTime()));

        when(mockedScriptExecutor.executeAndReturnResult()).thenReturn(100L);

        assertEquals(metric.getValue(context), 100L);
        verify(mockedScriptExecutor, times(1)).executeAndReturnResult();

        assertEquals(metric.loadValue(context), 100L);

        // if value is stored all subsequent invocation will not execute queries
        assertEquals(metric.getValue(context), 100L);
        verify(mockedScriptExecutor, times(1)).executeAndReturnResult();
    }

    /**
     * For testing purpose only.
     */
    class TestedMetric extends ScriptBasedMetric {

        private final ScriptExecutor scriptExecutor;

        public TestedMetric(ScriptExecutor scriptExecutor, MetricType metricType) {
            super(metricType);
            this.scriptExecutor = scriptExecutor;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        protected ScriptType getScriptType() {
            return ScriptType.EVENT_COUNT_WORKSPACE_CREATED;
        }

        @Override
        protected ScriptExecutor getScriptExecutor(ScriptType scriptType) {
            return scriptExecutor;
        }
    }
}
