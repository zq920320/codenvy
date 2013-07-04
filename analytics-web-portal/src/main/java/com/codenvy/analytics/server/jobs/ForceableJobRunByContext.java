/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import java.util.Map;

/**
 * The job should implement this interface if it supports job running
 * dependently of context.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface ForceableJobRunByContext {

    /**
     * Runs job.
     *
     * @param context the execution context
     * @throws Exception
     */
    void forceRun(Map<String, String> context) throws Exception;
}
