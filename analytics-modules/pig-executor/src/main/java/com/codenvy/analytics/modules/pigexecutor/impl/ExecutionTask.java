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
package com.codenvy.analytics.modules.pigexecutor.impl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.codenvy.analytics.modules.pigexecutor.PigExecutorService;
import com.codenvy.analytics.modules.pigexecutor.config.ExecutionEntry;
import com.codenvy.analytics.modules.pigexecutor.config.ScriptEntry;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ExecutionTask implements Job {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            PigExecutorService executor = PigExecutorServiceImpl.getInstance();

            String key = context.getJobDetail().getKey().toString() + "_" + context.getTrigger().getKey().toString();
            
            ExecutionEntry executionEntry = executor.getScheduledTask(key);
            
            for (ScriptEntry scriptEntry : executionEntry.getScripts())
            {
                executor.execute(scriptEntry);
            }
        } catch (Exception e) {
            throw new JobExecutionException(e.getMessage(), e);
        }
    }
}
