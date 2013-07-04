/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

/**
 * The job should implement this interface if it supports job running
 * independently of context.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface ForceableRunOnceJob {

    /**
     * Runs job.
     * @throws Exception
     */
    void forceRun() throws Exception;
}
