/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.server.TimeLineViewServiceImpl;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineViewJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLineViewJob.class);

    /**
     * @return initialized job
     */
    public static JobDetail createJob() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(TimeLineViewJob.class.getName()));
        jobDetail.setJobClass(TimeLineViewJob.class);

        return jobDetail;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("TimeLineViewJob is started");
        long start = System.currentTimeMillis();

        try {
            new TimeLineViewServiceImpl().getViews(new Date(), TimeUnit.DAY);
            new TimeLineViewServiceImpl().getViews(new Date(), TimeUnit.WEEK);
            new TimeLineViewServiceImpl().getViews(new Date(), TimeUnit.WEEK);
        } finally {
            LOGGER.info("TimeLineViewJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
