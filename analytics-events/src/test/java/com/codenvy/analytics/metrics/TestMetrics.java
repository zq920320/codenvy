/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codenvy.analytics.scripts.FileObject;
import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ScriptType;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestMetrics {

    private ScriptExecutor      mockedScriptExecutor;
    private FileObject          mockedFileObject;
    private AbstractMetric      metric;
    private Map<String, String> context;

    @BeforeMethod
    public void setUp() throws Exception {
        mockedScriptExecutor = mock(ScriptExecutor.class);
        mockedFileObject = mock(FileObject.class);
        metric = new TestedWorkspaceCreatedMetric(mockedScriptExecutor);
        context = new HashMap<String, String>();
    }

    /**
     * Calculated value should not be stored since {@link ScriptParameters#TO_DATE} is current date.
     */
    @Test(expectedExceptions = IOException.class)
    public void shouldNotStoreValue() throws Exception {
        when(mockedFileObject.getValue()).thenReturn("100");
        when(mockedScriptExecutor.executeAndReturnResult()).thenReturn(mockedFileObject);

        context.put(ScriptParameters.FROM_DATE.getName(), "20130301");
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.paramDateFormat.format(Calendar.getInstance().getTime()));

        assertEquals(metric.getValue(context), "100");
        
        // expects throwing exception, since file is absent
        metric.getScriptType().createFileObject(ScriptExecutor.RESULT_DIRECTORY, context);
    }

    /**
     * Calculated value will be stored since {@link ScriptParameters#TO_DATE} is less than current date.
     */
    @Test
    public void shouldStoreValue() throws Exception {
        Calendar prevDate = Calendar.getInstance();
        prevDate.add(Calendar.DAY_OF_MONTH, -1);

        context.put(ScriptParameters.FROM_DATE.getName(), "20130301");
        context.put(ScriptParameters.TO_DATE.getName(), ScriptExecutor.paramDateFormat.format(prevDate.getTime()));

        FileObject fileObject = metric.getScriptType().createFileObject(ScriptExecutor.RESULT_DIRECTORY, context, 100L);

        when(mockedScriptExecutor.executeAndReturnResult()).thenReturn(fileObject);

        assertEquals(metric.getValue(context), "100");

        // value will be loaded from storage
        fileObject = metric.getScriptType().createFileObject(ScriptExecutor.RESULT_DIRECTORY, context);
        assertEquals(fileObject.getValue(), 100L);

        verify(mockedScriptExecutor, times(1)).executeAndReturnResult();

        // if value is stored all subsequent invocation will not execute queries
        metric.getValue(context);
        verify(mockedScriptExecutor, times(1)).executeAndReturnResult();
    }

    /**
     * For testing purpose only.
     */
    class TestedWorkspaceCreatedMetric extends AbstractMetric {

        private final ScriptExecutor scriptExecutor;

        public TestedWorkspaceCreatedMetric(ScriptExecutor scriptExecutor) {
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
