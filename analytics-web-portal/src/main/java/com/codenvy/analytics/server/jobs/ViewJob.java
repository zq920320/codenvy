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


package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.server.AnalysisServiceImpl;
import com.codenvy.analytics.server.FactoryUrlTimeLineServiceImpl;
import com.codenvy.analytics.server.TimeLineServiceImpl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ViewJob implements Job, ForceableRunOnceJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewJob.class);

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
        LOGGER.info("ViewJob is started");
        long start = System.currentTimeMillis();

        try {
            new TimeLineServiceImpl().update();
            new FactoryUrlTimeLineServiceImpl().update();
            new AnalysisServiceImpl().update();
        } finally {
            LOGGER.info("ViewJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
