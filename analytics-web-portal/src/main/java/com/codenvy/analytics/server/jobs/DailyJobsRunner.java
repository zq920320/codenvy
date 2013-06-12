/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import org.quartz.CronScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

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
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            LOGGER.error(e.getMessage(), e);
        }
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

            scheduler.scheduleJob(TimeLineViewJob.createJob(), makeTrigger());
            scheduler.scheduleJob(AnalysisViewJob.createJob(), makeTrigger());
            scheduler.scheduleJob(UsersProfilePreparation.createJob(), makeTrigger());
            scheduler.scheduleJob(UsersActivityPreparation.createJob(), makeTrigger());

            try {
                ActOnJob actOnJob = new ActOnJob();
                scheduler.scheduleJob(actOnJob.getJobDetail(), actOnJob.getTrigger());
            } catch (FileNotFoundException e) {
                LOGGER.warn("Configuration for ActOnJob doesn't exist.");
            }

            try {
                JRebelJob jRebelJob = new JRebelJob();
                scheduler.scheduleJob(jRebelJob.getJobDetail(), jRebelJob.getTrigger());
            } catch (Exception e) {
                LOGGER.warn("Configuration for JRebelJob doesn't exist.");
            }
        } catch (Exception e) {
            LOGGER.error("Scheduler was not initialized", e);
        }
    }

    private Trigger makeTrigger() {
        return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION)).build();
    }
}
