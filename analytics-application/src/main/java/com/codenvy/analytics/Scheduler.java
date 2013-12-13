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


package com.codenvy.analytics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.backup.MongoDBBackupJob;
import com.codenvy.analytics.services.pig.PigRunner;
import com.codenvy.analytics.services.view.CSVReportPersister;
import com.codenvy.analytics.services.view.ViewBuilder;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Scheduler implements ServletContextListener {

    private static final Logger LOGGER                      = LoggerFactory.getLogger(Scheduler.class);
    private static final String FEATURE_FORCE_RUN_CONDITION = "feature.force.run.condition";
    private static final String FEATURE_FORCE_RUN_CLASS     = "feature.force.run.class";
    private static final String FEATURES_CRON_TIMETABLE     = "features.cron.timetable";

    private static final String FORCE_RUN_CONDITION_ALLTIME = "ALLTIME";
    private static final String FORCE_RUN_CONDITION_LASTDAY = "LASTDAY";

    private org.quartz.Scheduler scheduler;

    private static final Class[] features = new Class[]{PigRunner.class, ViewBuilder.class, MongoDBBackupJob.class};

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
        try {
            CSVReportPersister.restoreBackup();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        initializeScheduler();

        String forceRunCondition = Configurator.getString(FEATURE_FORCE_RUN_CONDITION);
        if (forceRunCondition != null) {
            forceRunJobs();
        }
    }

    private void forceRunJobs() {
        try {
            String forceRunCondition = Configurator.getString(FEATURE_FORCE_RUN_CONDITION);
            String forceRunFeature = Configurator.getString(FEATURE_FORCE_RUN_CLASS);

            for (Class jobClass : features) {
                Feature job = (Feature)jobClass.getConstructor().newInstance();

                if (forceRunFeature == null || forceRunFeature.equals(job.getClass().getName())) {
                    switch (forceRunCondition.toUpperCase()) {
                        case FORCE_RUN_CONDITION_LASTDAY:
                            executeLastDay(job);
                            break;

                        case FORCE_RUN_CONDITION_ALLTIME:
                            executeAllTime(job);
                            break;

                        default:
                            executeSpecificPeriod(job, forceRunCondition);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to force job run: " + e.getMessage(), e);
        }
    }

    private void executeSpecificPeriod(Feature job, String runCondition) throws Exception {
        if (runCondition.contains(",")) {
            String[] dates = runCondition.split(",");
            execute(job, dates[0], dates[1]);
        } else {
            execute(job, runCondition, runCondition);
        }
    }

    private void executeAllTime(Feature job) throws Exception {
        execute(job, Parameters.FROM_DATE.getDefaultValue(), Parameters.TO_DATE.getDefaultValue());
    }

    private void execute(Feature job, String fromDateParam, String toDateParam) throws Exception {
        Map<String, String> context = Utils.newContext();

        Calendar fromDate = Utils.parseDate(fromDateParam);
        Calendar toDate = Utils.parseDate(toDateParam);

        if (fromDate.after(toDate)) {
            throw new IllegalStateException("FROM_DATE Parameters is bigger than TO_DATE Parameters");
        }

        Utils.putFromDate(context, fromDate);
        Utils.putToDate(context, fromDate);
        Parameters.TIME_UNIT.put(context, Parameters.TimeUnit.DAY.name());

        do {
            job.forceExecute(context);
            context = Utils.nextDateInterval(context);
        } while (!Utils.getFromDate(context).after(toDate));
    }

    private void executeLastDay(Feature job) throws Exception {
        execute(job, Parameters.TO_DATE.getDefaultValue(), Parameters.TO_DATE.getDefaultValue());
    }

    /** Creates scheduler and adds available jobs. */
    public void initializeScheduler() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();

            List<JobDetail> jobDetails = new ArrayList<>(2);

            for (Class feature : features) {
                addJobDetail(feature, jobDetails);
            }

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
        return TriggerBuilder.newTrigger().withSchedule(
                CronScheduleBuilder.cronSchedule(Configurator.getString(FEATURES_CRON_TIMETABLE))).build();
    }

    private void addJobDetail(Class<? extends Feature> clazz, List<JobDetail> jobDetails) {
        if (isAvailable(clazz)) {
            JobDetailImpl jobDetail = new JobDetailImpl();
            jobDetail.setKey(new JobKey(clazz.getName()));
            jobDetail.setJobClass(clazz);
            jobDetail.setDurability(true);

            jobDetails.add(jobDetail);
        }
    }

    /**
     * @throws IllegalStateException
     *         if class can't be instantiated
     * @see com.codenvy.analytics.services.Feature#isAvailable()
     */
    private boolean isAvailable(Class<? extends Feature> clazz) throws IllegalStateException {
        try {
            Feature extendedJob = clazz.getConstructor().newInstance();
            return extendedJob.isAvailable();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
