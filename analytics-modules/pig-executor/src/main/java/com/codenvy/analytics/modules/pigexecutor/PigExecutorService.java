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
package com.codenvy.analytics.modules.pigexecutor;

import com.codenvy.analytics.modules.pigexecutor.config.ExecutionEntry;
import com.codenvy.analytics.modules.pigexecutor.config.PigScriptsExecutorConfiguration;
import com.codenvy.analytics.modules.pigexecutor.config.ScriptEntry;


/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public interface PigExecutorService {
    
    /** Runtime parameter name. Contains the configuration of PigExecutorService. */
    public static final String ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY = "analytics.pig.executor.service.congif";

    /** The value of {@value #ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY} runtime parameter. */
    public static final String PIG_EXECUTOR_CONFIG = System.getProperty(ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY);
    
    /**
     * Schedule of executing the scripts which configured in ExecutionEntry.
     * 
     * @param executionEntry
     *           ExecutionEntry
     * @throws ShedulingExecutionEntryException
     */
    void schedule(ExecutionEntry executionEntry) throws ShedulingExecutionEntryException;
    
    /**
     * Executing scripts which configured in ScriptEntry.
     * 
     * @param scriptEntry
     *           ScriptEntry
     */
    void execute(ScriptEntry scriptEntry);
    
    /**
     * Get the scheduled for execute ExecutionEntry by KEY
     * 
     * @param key
     *           String
     * @return ExecutionEntry
     *            return the scheduled for execute ExecutionEntry
     * @throws ShedulingExecutionEntryException
     */
    ExecutionEntry getScheduledTask(String key) throws ShedulingExecutionEntryException;
    
    /**
     * Get the configuration of PigExecutorService.
     * 
     * @return PigScriptsExecutorConfiguration
     */
    PigScriptsExecutorConfiguration getConfiguration();
    
    /**
     * All scheduled tasks will be shutdown. 
     */
    void shutdown();
}
