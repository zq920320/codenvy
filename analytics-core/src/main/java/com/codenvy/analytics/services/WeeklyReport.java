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
package com.codenvy.analytics.services;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.view.CSVReportPersister;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

public class WeeklyReport implements Feature {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklyReport.class);

    private static final String AVAILABLE    = "weekly-report.available";
    private static final String MAIL_TEXT    = "weekly-report.mail.text";
    private static final String MAIL_SUBJECT = "weekly-report.mail.subject";
    private static final String MAIL_TO      = "weekly-report.mail.to";

    private final CSVReportPersister reportPersister;

    public WeeklyReport() {
        this.reportPersister = new CSVReportPersister();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            doExecute(Utils.initializeContext(Parameters.TimeUnit.DAY));
        } catch (ParseException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void forceExecute(Map<String, String> context) throws JobExecutionException {
        try {
            doExecute(context);
        } catch (ParseException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private void doExecute(Map<String, String> context) throws IOException, ParseException {
        LOG.info("WeeklyReport is started");
        long start = System.currentTimeMillis();

        try {
            if (isFirstDayOfWeek()) {
                File report = reportPersister.getReport("timeline_week", context);
                if (!report.exists()) {
                    throw new IOException("Report not found");
                }

                sendReport(report);
            }
        } finally {
            LOG.info("WeeklyReport is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private boolean isFirstDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) ==
               Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_WEEK);
    }

    protected void sendReport(File reportFile) throws IOException {
        MailService.Builder builder = new MailService.Builder();
        builder.attach(reportFile);
        builder.setSubject(Configurator.getString(MAIL_SUBJECT));
        builder.setText(Configurator.getString(MAIL_TEXT));
        builder.setTo(Configurator.getString(MAIL_TO));
        MailService mailService = builder.build();

        mailService.send();
    }

    @Override
    public boolean isAvailable() {
        return Configurator.getBoolean(AVAILABLE);
    }
}
