/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.server.AnalysisServiceImpl;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalysisViewJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisViewJob.class);

    /**
     * @return initialized job
     */
    public static JobDetail createJob() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(AnalysisViewJob.class.getName()));
        jobDetail.setJobClass(AnalysisViewJob.class);

        return jobDetail;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("AnalysisViewJob is started");
        long start = System.currentTimeMillis();

        try {
            new AnalysisServiceImpl().update();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("AnalysisViewJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
