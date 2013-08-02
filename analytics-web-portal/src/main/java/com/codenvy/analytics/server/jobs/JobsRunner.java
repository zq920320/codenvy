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

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class JobsRunner implements ServletContextListener {

    private static final Logger LOGGER                             = LoggerFactory.getLogger(JobsRunner.class);
    private static final String ANALYTICS_FORCE_RUN_JOBS_CONDITION = "analytics.force.run.jobs.condition";
    private static final String ANALYTICS_FORCE_RUN_JOBS_CLASS     = "analytics.force.run.jobs.class";
    private static final String CRON_TIMETABLE                     = "0 0 1 ? * *";

    private static final String FORCE_RUN_CONDITION_ALLTIME = "ALLTIME";
    private static final String FORCE_RUN_CONDITION_ONCE    = "ONCE";
    private static final String FORCE_RUN_CONDITION_LASTDAY = "LASTDAY";

    private Scheduler scheduler;

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

        String forceRunCondition = System.getProperty(ANALYTICS_FORCE_RUN_JOBS_CONDITION);
        if (forceRunCondition != null) {
            forceRunJobs(forceRunCondition);
        }
    }

    private void forceRunJobs(String forceRunCondition) {
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Scheduler.DEFAULT_GROUP));

            String forceRunClass = System.getProperty(ANALYTICS_FORCE_RUN_JOBS_CLASS);
            for (JobKey key : jobKeys) {
                Class<? extends Job> jobClass = scheduler.getJobDetail(key).getJobClass();
                Job job = jobClass.getConstructor().newInstance();

                if (forceRunClass == null || forceRunClass.equals(jobClass.getName())) {
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
            Map<String, String> context = Utils.initializeContext(TimeUnit.DAY);
            ((ForceableJobRunByContext)job).forceRun(context);
        }
    }

    private void executeOnce(Job job) throws Exception {
        if (job instanceof ForceableRunOnceJob) {
            ((ForceableRunOnceJob)job).forceRun();
        }
    }

    /** Creates scheduler and adds available jobs. */
    public void initializeScheduler() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();

            List<JobDetail> jobDetails = new ArrayList<>(6);

            addJobDetail(UsersDataJob.class, jobDetails);
            addJobDetail(TimeLineViewJob.class, jobDetails);
            addJobDetail(AnalysisViewJob.class, jobDetails);
            addJobDetail(ActOnJob.class, jobDetails);
            addJobDetail(JRebelJob.class, jobDetails);
            addJobDetail(CheckLogsJob.class, jobDetails);

            scheduleJobs(jobDetails);
            ensureRunOrder(jobDetails);

            scheduler.start();
        } catch (Exception e) {
            LOGGER.error("Scheduler was not initialized properly", e);
        }
    }

    private void scheduleJobs(List<JobDetail> jobDetails) throws SchedulerException {
        scheduler.scheduleJob(jobDetails.get(0), createTrigger());
        LOGGER.info(jobDetails.get(0).getJobClass() + " initialized");

        for (int i = 1; i < jobDetails.size(); i++) {
            scheduler.addJob(jobDetails.get(i), true);
            LOGGER.info(jobDetails.get(i).getJobClass() + " initialized");
        }
    }

    private void ensureRunOrder(List<JobDetail> jobDetails) throws SchedulerException {
        JobChainingJobListener listener = new JobChainingJobListener("listener");

        for (int i = 1; i < jobDetails.size(); i++) {
            listener.addJobChainLink(jobDetails.get(i - 1).getKey(), jobDetails.get(i).getKey());
        }

        scheduler.getListenerManager().addJobListener(listener);
    }

    private CronTrigger createTrigger() {
        return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(CRON_TIMETABLE)).build();
    }

    private void addJobDetail(Class<? extends Job> clazz, List<JobDetail> jobDetails) {
        if (isAccessable(clazz)) {
            JobDetailImpl jobDetail = new JobDetailImpl();
            jobDetail.setKey(new JobKey(clazz.getName()));
            jobDetail.setJobClass(clazz);

            jobDetails.add(jobDetail);
        }
    }

    private boolean isAccessable(Class<? extends Job> clazz) {
        try {
            clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            return false;
        }

        return true;
    }
}
