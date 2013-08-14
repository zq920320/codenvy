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

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.FactoryUrlTimeLineServiceImpl;
import com.codenvy.analytics.server.TimeLineServiceImpl;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ViewsJob implements Job, ForceableRunOnceJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewsJob.class);

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
        LOGGER.info("ViewsJob is started");
        long start = System.currentTimeMillis();

        try {
            calculateTimeLineData();
            calculateFactoryTimeLineData();
        } finally {
            LOGGER.info("ViewsJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void calculateFactoryTimeLineData() throws Exception {
        FactoryUrlTimeLineServiceImpl service = new FactoryUrlTimeLineServiceImpl();

        Map<String, String> context = Utils.initializeContext(TimeUnit.DAY);
        service.calculateAndSave(context);

        context = Utils.initializeContext(TimeUnit.WEEK);
        service.calculateAndSave(context);

        context = Utils.initializeContext(TimeUnit.MONTH);
        service.calculateAndSave(context);

        context = Utils.initializeContext(TimeUnit.LIFETIME);
        service.calculateAndSave(context);
    }

    private void calculateTimeLineData() throws Exception {
        TimeLineServiceImpl service = new TimeLineServiceImpl();

        Map<String, String> context = Utils.initializeContext(TimeUnit.DAY);
        service.calculateAndSave(context);

        context = Utils.initializeContext(TimeUnit.WEEK);
        service.calculateAndSave(context);

        context = Utils.initializeContext(TimeUnit.MONTH);
        service.calculateAndSave(context);

        context = Utils.initializeContext(TimeUnit.LIFETIME);
        service.calculateAndSave(context);
    }
}
