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

import java.text.DateFormat;
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

    private static final Logger LOGGER                       = LoggerFactory.getLogger(JobsRunner.class);
    private static final String ANALYTICS_FORCE_RUN_JOBS     = "analytics.force.run.jobs";
    private static final String FORCE_RUN_CONDITION_LIFETIME = "LIFETIME";
    private static final String CRON_TIMETABLE               = "0 0 1 ? * *";

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
            // TODO not all jobs
        }
    }

    private void forceRunJobs(String forceRunCondition) {
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Scheduler.DEFAULT_GROUP));

            for (JobKey jobKey : jobKeys) {
                Job job = scheduler.getJobDetail(jobKey).getJobClass().getConstructor().newInstance();

                if (job instanceof ForceableJob) {
                    switch (forceRunCondition.toUpperCase()) {
                        case FORCE_RUN_CONDITION_LIFETIME:
                            executeAllTime((ForceableJob)job);
                            break;

                        default:
                            executeOnce((ForceableJob)job, forceRunCondition);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to force run jobs: " + e.getMessage(), e);
        }
    }

    private void executeAllTime(ForceableJob job) throws Exception {
        Map<String, String> context = Utils.initializeContext(TimeUnit.DAY, new Date());

        do {
            job.forceRun(context);
            context = Utils.prevDateInterval(context);
        } while (!MetricParameter.FROM_DATE.getDefaultValue().equals(Utils.getFromDateParam(context)));
    }

    private void executeOnce(ForceableJob job, String forceRunCondition) throws Exception {
        DateFormat df = new SimpleDateFormat(MetricParameter.PARAM_DATE_FORMAT);
        Date date = df.parse(forceRunCondition);
        
        Map<String, String> context = Utils.newContext();
        Utils.putFromDate(context, date);
        Utils.putToDate(context, date);

        job.forceRun(context);
    }

    /**
     * Creates scheduler and adds available jobs.
     */
    public void initializeScheduler() {
        try {
            overrideSchedulerProperties();

            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();

            initializeJob(TimeLineViewJob.class);
            initializeJob(AnalysisViewJob.class);
            initializeJob(UsersDataJob.class);
            initializeJob(ActOnJob.class);
            initializeJob(JRebelJob.class);

        } catch (Exception e) {
            LOGGER.error("Scheduler was not initialized properly", e);
        }
    }

    private void overrideSchedulerProperties() {
        System.setProperty("org.quartz.threadPool.threadCount", "1"); // don't touch by dirty hands
    }

    private void initializeJob(Class< ? extends Job> clazz) throws SchedulerException {
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
