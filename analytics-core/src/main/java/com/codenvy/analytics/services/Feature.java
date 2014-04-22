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


package com.codenvy.analytics.services;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/**
 * Extended interface for {@link Job}
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class Feature implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(Feature.class);

    /**
     * Forcing job execution.
     *
     * @param context
     *         the execution context
     * @throws JobExecutionException
     */
    public void forceExecute(Context context) throws JobExecutionException {
        try {
            if (!context.getAsString(Parameters.FROM_DATE).equals(context.getAsString(Parameters.TO_DATE))) {
                throw new IllegalStateException("Force execution is allowed only per day");
            }

            Context.Builder builder = new Context.Builder(context);
            doExecute(builder.build());
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            Context.Builder builder = initializeContextBuilder();
            doExecute(builder.build());
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** Initialize context if job is being executed on regular basis */
    protected Context.Builder initializeContextBuilder() throws ParseException {
        Context.Builder builder = new Context.Builder();
        builder.putDefaultValue(Parameters.TO_DATE);
        builder.put(Parameters.FROM_DATE, Parameters.TO_DATE.getDefaultValue());

        return builder;
    }

    /**
     * Execution.
     *
     * @param context
     *         the execution context
     */
    protected abstract void doExecute(Context context) throws Exception;

    /** @return true if feature can be executed on regular basis. */
    public abstract boolean isAvailable();
}
