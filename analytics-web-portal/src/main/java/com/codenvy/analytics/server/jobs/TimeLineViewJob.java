/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.server.TimeLineServiceImpl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineViewJob implements Job, ForceableRunOnceJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeLineViewJob.class);

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
        LOGGER.info("TimeLineViewJob is started");
        long start = System.currentTimeMillis();

        try {
            TimeLineServiceImpl service = new TimeLineServiceImpl();

            service.update(TimeUnit.DAY);
            service.update(TimeUnit.WEEK);
            service.update(TimeUnit.MONTH);
        } finally {
            LOGGER.info("TimeLineViewJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
