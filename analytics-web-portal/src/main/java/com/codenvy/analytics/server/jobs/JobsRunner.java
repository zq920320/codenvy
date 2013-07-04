/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JobsRunner implements ServletContextListener {

    private static final Logger LOGGER                      = LoggerFactory.getLogger(JobsRunner.class);
    private static final String ANALYTICS_FORCE_RUN_JOBS    = "analytics.force.run.jobs";
    private static final String CRON_TIMETABLE              = "0 0 1 ? * *";

    private static final String FORCE_RUN_CONDITION_ALLTIME = "ALLTIME";
    private static final String FORCE_RUN_CONDITION_ONCE    = "ONCE";
    private static final String FORCE_RUN_CONDITION_LASTDAY = "LASTDAY";

    private Scheduler           scheduler;

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent context) {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent context) {
        initializeScheduler();

        String forceRunCondition = System.getProperty(ANALYTICS_FORCE_RUN_JOBS);
        if (forceRunCondition != null) {
            forceRunJobs(forceRunCondition);
        }
    }

    private void forceRunJobs(String forceRunCondition) {
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Scheduler.DEFAULT_GROUP));

            for (JobKey key : jobKeys) {
                Job job = scheduler.getJobDetail(key).getJobClass().getConstructor().newInstance();

                switch (forceRunCondition.toUpperCase()) {
                    case FORCE_RUN_CONDITION_LASTDAY:
                        executeLastDay(job);
                        break;

                    case FORCE_RUN_CONDITION_ONCE:
                        executeOnce(job);
                        break;

                    case FORCE_RUN_CONDITION_ALLTIME:
                        executeAllTime(job);
                        break;

                    default:
                        executeSpecificDay(job, forceRunCondition);
                        break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to force job run: " + e.getMessage(), e);
        }
    }

    private void executeAllTime(Job job) throws Exception {
        if (job instanceof ForceableJobRunByContext) {
            Map<String, String> context = Utils.newContext();
            context.put(MetricParameter.FROM_DATE.name(), MetricParameter.FROM_DATE.getDefaultValue());
            context.put(MetricParameter.TO_DATE.name(), MetricParameter.FROM_DATE.getDefaultValue());
            context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.name());

            do {
                ((ForceableJobRunByContext)job).forceRun(context);
                context = Utils.nextDateInterval(context);
            } while (!Utils.getToDateParam(context).equals(MetricParameter.TO_DATE.getDefaultValue()));

            executeLastDay(job);
        }
    }

    private void executeSpecificDay(Job job, String forceRunCondition) throws Exception {
        if (job instanceof ForceableJobRunByContext) {
            SimpleDateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);
            df.parse(forceRunCondition);

            Map<String, String> context = Utils.newContext();
            context.put(MetricParameter.FROM_DATE.name(), forceRunCondition);
            context.put(MetricParameter.TO_DATE.name(), forceRunCondition);
            context.put(MetricParameter.TIME_UNIT.name(), TimeUnit.DAY.name());

            ((ForceableJobRunByContext)job).forceRun(context);
        }
    }

    private void executeLastDay(Job job) throws Exception {
        if (job instanceof ForceableJobRunByContext) {
            Map<String, String> context = Utils.initializeContext(TimeUnit.DAY, new Date());
            ((ForceableJobRunByContext)job).forceRun(context);
        }
    }

    private void executeOnce(Job job) throws Exception {
        if (job instanceof ForceableRunOnceJob) {
            ((ForceableRunOnceJob)job).forceRun();
        }
    }

    /**
     * Creates scheduler and adds available jobs.
     */
    public void initializeScheduler() {
        try {
            setDefaultSchedulerProperties();

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();

            initializeJob(UsersDataJob.class);
            initializeJob(TimeLineViewJob.class);
            initializeJob(AnalysisViewJob.class);
            initializeJob(ActOnJob.class);
            initializeJob(JRebelJob.class);

        } catch (Exception e) {
            LOGGER.error("Scheduler was not initialized properly", e);
        }
    }

    private void setDefaultSchedulerProperties() {
        System.setProperty("org.quartz.threadPool.threadCount", "1"); // to sure run order
    }

    private void initializeJob(Class< ? extends Job> clazz) throws SchedulerException {
        try {
            clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
            return;
        }

        scheduler.scheduleJob(creeteJobDetail(clazz), createTrigger());
        LOGGER.info(clazz.getName() + " initialized");
    }

    private CronTrigger createTrigger() {
        return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(CRON_TIMETABLE)).build();
    }

    private JobDetail creeteJobDetail(Class< ? extends Job> clazz) {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(clazz.getName()));
        jobDetail.setJobClass(clazz);

        return jobDetail;
    }
}
