/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.FileObject;
import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ScriptType;

import org.apache.pig.backend.executionengine.ExecException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class AbstractMetric implements Metric {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(Map<String, String> context) throws IOException {
        ScriptType scriptType = getScriptType();

        try {
            return tryExistedValue(scriptType, context);
        } catch (IOException e) {
            return tryQueryResult(scriptType, context);
        }
    }

    private String tryQueryResult(ScriptType scriptType, Map<String, String> context) throws IOException
    {
        ScriptExecutor scriptExecutor = getScriptExecutor(scriptType);
        scriptExecutor.setParams(context);

        FileObject fileObject = scriptExecutor.executeAndReturnResult();
        storeIfAllowed(scriptType, fileObject, context);

        return fileObject.getValue().toString();
    }

    private String tryExistedValue(ScriptType scriptType, Map<String, String> context) throws IOException {
        FileObject fileObject = scriptType.createFileObject(ScriptExecutor.RESULT_DIRECTORY, context);
        return fileObject.getValue().toString();
    }

    private void storeIfAllowed(ScriptType scriptType, FileObject fileObject, Map<String, String> context) throws IOException {
        if (scriptType.isStoreSupport()) {
            String toDateParam = context.get(ScriptParameters.TO_DATE.getName());

            if (toDateParam != null) {
                Calendar toDate = Calendar.getInstance();
                try {
                    toDate.setTime(ScriptExecutor.paramDateFormat.parse(toDateParam));
                } catch (ParseException e) {
                    throw new IOException(e);
                }
                
                Calendar currentDate = Calendar.getInstance();
                currentDate.set(Calendar.MILLISECOND, 0);
                currentDate.set(Calendar.SECOND, 0);
                currentDate.set(Calendar.MINUTE, 0);
                currentDate.set(Calendar.HOUR_OF_DAY, 0);
                
                if (currentDate.after(toDate)) {
                    fileObject.store();
                }
            }
        }
    }

    /**
     * @return {@link ScriptExecutor} instance
     */
    protected ScriptExecutor getScriptExecutor(ScriptType scriptType) throws ExecException
    {
        return new ScriptExecutor(scriptType);
    }

    /**
     * @return {@link ScriptType}
     */
    abstract protected ScriptType getScriptType();
}
