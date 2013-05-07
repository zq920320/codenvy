/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server;

import com.codenvy.analytics.server.jobs.ActOnJob;
import com.codenvy.analytics.server.jobs.TimeLineViewJob;

import org.quartz.CronScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class DailyJobsRunner implements ServletContextListener {

    private static final Logger LOGGER          = LoggerFactory.getLogger(DailyJobsRunner.class);
    private static final String CRON_EXPRESSION = "0 0 1 ? * *";
    public static Scheduler     scheduler;


    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent arg0) {
        init();
    }

    public void init() {
        try {
            System.setProperty("org.quartz.threadPool.threadCount", "1");

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();

            scheduler.scheduleJob(TimeLineViewJob.createJob(), createTrigger());
            scheduler.scheduleJob(ActOnJob.createJob(), createTrigger());
        } catch (Exception e) {
            LOGGER.error("Scheduler was not initialized", e);
        }
    }

    /**
     * Creates trigger. Will be run every day at 01:00.
     */
    private Trigger createTrigger() {
        return TriggerBuilder
                             .newTrigger()
                             .withSchedule(
                                           CronScheduleBuilder.cronSchedule(CRON_EXPRESSION))
                             .build();
    }
}
