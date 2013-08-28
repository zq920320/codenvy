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

import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.server.PersisterUtil;
import com.codenvy.analytics.server.service.MailService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

public class WeeklyReportJob implements Job {
    private static final Logger LOGGER                   = LoggerFactory.getLogger(WeeklyReportJob.class);
    private static final String WEEKLY_REPORT_PROPERTIES = System.getProperty("analytics.job.weekly.report.properties");
    private final Properties weeklyReportProperties;

    public WeeklyReportJob() throws IOException {
        this.weeklyReportProperties = Utils.readProperties(WEEKLY_REPORT_PROPERTIES);
    }

    public WeeklyReportJob(Properties properties) throws IOException {
        this.weeklyReportProperties = properties;
    }

    /** {@inheritDoc} */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            run();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private void run() throws IOException {
        LOGGER.info(this.getClass().getName() + " is started");
        long start = System.currentTimeMillis();

        try {
            if (isFirstDayOfWeek()) {
                File file = PersisterUtil.getCsvFile(System.getProperty("analytics.csv.reports.directory"), "timeline_week.csv");
                sendMail(file);
            }
        } finally {
            LOGGER.info(this.getClass().getName() + " is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private boolean isFirstDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_WEEK);
    }

    protected void sendMail(File file) throws IOException {
        MailService mailService = new MailService(weeklyReportProperties);
        mailService.send(file);
    }
}
