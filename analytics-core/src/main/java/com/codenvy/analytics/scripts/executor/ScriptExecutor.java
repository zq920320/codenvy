/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.scripts.executor;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface ScriptExecutor {

    public static final ScriptExecutor INSTANCE = new PigScriptExecutor();

    /**
     * Run the script and returns the result.
     * 
     * @param scriptType specific script type to execute
     * @param context contains all necessary value parameters required but given {@link ScriptType}
     * @throws IOException if something gone wrong or if a required parameter is absent
     */
    ValueData executeAndReturn(ScriptType scriptType, Map<String, String> context) throws IOException;

}