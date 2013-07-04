/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.server.AnalysisServiceImpl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalysisViewJob implements Job, ForceableRunOnceJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisViewJob.class);

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            run();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** {@inheritDoc} */
    public void forceRun() throws Exception {
        run();
    }

    private void run() throws Exception {
        LOGGER.info("AnalysisViewJob is started");
        long start = System.currentTimeMillis();

        try {
            AnalysisServiceImpl service = new AnalysisServiceImpl();
            service.update();
        } finally {
            LOGGER.info("AnalysisViewJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
