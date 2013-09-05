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

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.MapStringLongValueData;
import com.codenvy.analytics.server.service.MailService;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ErrorsReportJob implements Job {

    private static final Logger LOGGER                     = LoggerFactory.getLogger(ErrorsReportJob.class);
    private static final String ERRORS_REPORT_PROPERTIES_RESOURCE = System.getProperty("analytics.job.errors.report.properties");

    private final Properties    monthlyErrorsReportProperties;

    public ErrorsReportJob() throws IOException {
        this.monthlyErrorsReportProperties = Utils.readProperties(ERRORS_REPORT_PROPERTIES_RESOURCE);
    }

    /** {@inheritDoc} */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // This report will be send every day, but list of errors should be for last 30 days.
            
            Map<String, String> executionContext = Utils.initializeContext(TimeUnit.DAY);

            Calendar newFromDate = Utils.getToDate(executionContext);
            newFromDate.add(Calendar.DAY_OF_MONTH, -29);
            MetricParameter.FROM_DATE.put(executionContext, Utils.formatDate(newFromDate));
            
            run(executionContext);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private void run(Map<String, String> context) throws IOException {
        LOGGER.info("ErrorsReportJob is started");
        long start = System.currentTimeMillis();

        try {
            StringBuilder errorsByErrorTypeReport = getErrorsByErrorType(context);
            String fomDate = MetricParameter.FROM_DATE.get(context);
            String toDate = MetricParameter.TO_DATE.get(context);

            sendMail(errorsByErrorTypeReport, fomDate, toDate);
        } finally {
            LOGGER.info("ErrorsReportJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void sendMail(StringBuilder errorsByErrorTypeReport, String fromDate, String toDate) throws IOException {
        MailService mailService = new MailService(monthlyErrorsReportProperties);
        mailService.setSubject("Errors by error type for last 30 days : " + fromDate + " to " + toDate);
        mailService.setText("Hi.\n\n" + errorsByErrorTypeReport.toString() + "\n\nBest regards, Analytics Team");
        mailService.send();
    }

    private StringBuilder getErrorsByErrorType(Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.ERRORS_BY_TYPE);
        MapStringLongValueData value = (MapStringLongValueData)metric.getValue(context);

        StringBuilder builder = new StringBuilder();
        for ( Entry<String, Long> errorType : value.getAll().entrySet()) {
            builder.append(errorType.toString());
            builder.append('\n');
        }

        return builder;
    }
}
