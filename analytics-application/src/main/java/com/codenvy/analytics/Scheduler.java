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
import com.codenvy.analytics.services.*;
import com.codenvy.analytics.services.view.CSVReportPersister;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Calendar;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class Scheduler implements ServletContextListener {

    private static final Logger LOG                              = LoggerFactory.getLogger(Scheduler.class);
    private static final String SCHEDULER_FORCE_RUN_PERIOD       = "analytics.scheduler.force_run_period";
    private static final String SCHEDULER_FORCE_RUN_CLASS        = "analytics.scheduler.force_run_class";
    private static final String SCHEDULER_FORCE_RUN_ASYNCHRONOUS = "analytics.scheduler.force_run_asynchronous";
    private static final String SCHEDULER_CRON_TIMETABLE         = "analytics.scheduler.cron_timetable";

    private static final String FORCE_RUN_CONDITION_ALLTIME = "ALLTIME";
    private static final String FORCE_RUN_CONDITION_LASTDAY = "LASTDAY";

    private final Configurator       configurator;
    private final CSVReportPersister csvReportPersister;

    private org.quartz.Scheduler scheduler;

    private static final Class[] features = new Class[]{PigRunnerFeature.class,
                                                        LogCheckerFeature.class,
                                                        ReportSenderFeature.class,
                                                        ViewBuilderFeature.class,
                                                        ActOnFeature.class};


    public Scheduler() {
        configurator = Injector.getInstance(Configurator.class);
        csvReportPersister = Injector.getInstance(CSVReportPersister.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent context) {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent context) {
        try {
            csvReportPersister.restoreBackup();

            initializeScheduler();
            scheduleAllFeatures();

            if (configurator.getString(SCHEDULER_FORCE_RUN_PERIOD) != null) {
                executeSpecificFeature();
            }
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    private void executeSpecificFeature() {
        Thread forceRunFeatureThread = new Thread() {
            @Override
            public void run() {
                forceRunFeatures();
            }
        };

        if (configurator.getBoolean(SCHEDULER_FORCE_RUN_ASYNCHRONOUS)) {
            forceRunFeatureThread.start();
        } else {
            forceRunFeatureThread.run();
        }
    }

    private void forceRunFeatures() {
        try {
            String forceRunPeriod = configurator.getString(SCHEDULER_FORCE_RUN_PERIOD);
            Set<String> forceRunFeature =
                    new HashSet<>(Arrays.asList(configurator.getString(SCHEDULER_FORCE_RUN_CLASS).split(",")));

            for (Class jobClass : features) {
                Feature job = (Feature)jobClass.getConstructor().newInstance();

                if (forceRunFeature.isEmpty() || forceRunFeature.contains(job.getClass().getName())) {
                    switch (forceRunPeriod.toUpperCase()) {
                        case FORCE_RUN_CONDITION_LASTDAY:
                            executeForLastDay(job);
                            break;

                        case FORCE_RUN_CONDITION_ALLTIME:
                            executeForAllTime(job);
                            break;

                        default:
                            executeForSpecificPeriod(job, forceRunPeriod);
                            break;
                    }
                }
            }
        } catch (Throwable e) {
            LOG.error("Unable to force job run: " + e.getMessage(), e);
        }
    }

    private void executeForSpecificPeriod(Feature job, String runCondition) throws Exception {
        if (runCondition.contains(",")) {
            String[] dates = runCondition.split(",");
            for (String date : dates) {
                doExecute(job, date, date);
            }
        } else if (runCondition.contains("-")) {
            String[] dates = runCondition.split("-");
            doExecute(job, dates[0], dates[1]);
        } else {
            doExecute(job, runCondition, runCondition);
        }
    }

    private void executeForAllTime(Feature job) throws Exception {
        doExecute(job, Parameters.FROM_DATE.getDefaultValue(), Parameters.TO_DATE.getDefaultValue());
    }

    private void doExecute(Feature job, String fromDateParam, String toDateParam) throws Exception {
        Map<String, String> context = Utils.newContext();

        Calendar fromDate = Utils.parseDate(fromDateParam);
        Calendar toDate = Utils.parseDate(toDateParam);

        if (fromDate.after(toDate)) {
            throw new IllegalStateException("FROM_DATE Parameters is bigger than TO_DATE Parameters");
        }

        Utils.putFromDate(context, fromDate);
        Utils.putToDate(context, fromDate);
        do {
            job.forceExecute(context);

            fromDate.add(Calendar.DAY_OF_MONTH, 1);
            Utils.putFromDate(context, fromDate);
            Utils.putToDate(context, fromDate);
        } while (!Utils.getFromDate(context).after(toDate));
    }

    private void executeForLastDay(Feature job) throws Exception {
        doExecute(job, Parameters.TO_DATE.getDefaultValue(), Parameters.TO_DATE.getDefaultValue());
    }

    /** Creates scheduler and adds available jobs. */
    public void initializeScheduler() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
    }

    private void scheduleAllFeatures() throws SchedulerException {
        List<JobDetail> jobDetails = new ArrayList<>(2);

        for (Class feature : features) {
            addJobDetail(feature, jobDetails);
        }

        scheduleFeature(jobDetails);

        ensureRunOrder(jobDetails);
    }

    private void scheduleFeature(List<JobDetail> jobDetails) throws SchedulerException {
        scheduler.scheduleJob(jobDetails.get(0), createTrigger());
        LOG.info(jobDetails.get(0).getJobClass() + " initialized");

        for (int i = 1; i < jobDetails.size(); i++) {
            scheduler.addJob(jobDetails.get(i), true);
            LOG.info(jobDetails.get(i).getJobClass() + " initialized");
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
                CronScheduleBuilder.cronSchedule(configurator.getString(SCHEDULER_CRON_TIMETABLE))).build();
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
