/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import com.codenvy.analytics.server.service.MailService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class JRebelJob implements Job, ForceableJobRunByContext {

    private static final Logger LOGGER                     = LoggerFactory.getLogger(JRebelJob.class);
    private static final String JREBEL_PROPERTIES_RESOURCE = System.getProperty("analytics.job.jrebel.properties");

    private final Properties    jrebelProperties;

    public JRebelJob() throws IOException {
        this.jrebelProperties = readProperties();
    }

    /** {@inheritDoc} */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, String> executionContext = Utils.initializeContext(TimeUnit.DAY, new Date());
            run(executionContext);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forceRun(Map<String, String> context) throws Exception {
        run(context);
    }

    private void run(Map<String, String> context) throws IOException {
        LOGGER.info("JRebelJob is started");
        long start = System.currentTimeMillis();

        try {
            StringBuilder usersProfile = getUsersProfile(context);
            String date = Utils.getToDateParam(context);

            sendMail(usersProfile, date);
        } finally {
            LOGGER.info("JRebelJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void sendMail(StringBuilder usersProfile, String date) throws IOException {
        MailService mailService = new MailService(jrebelProperties);
        mailService.setSubject("JRebel user profiles on " + date);
        mailService.setText("Hi.\n\n" + usersProfile.toString() + "\n\nBest regards, Analytics Team");
        mailService.send();
    }

    private StringBuilder getUsersProfile(Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.createMetric(MetricType.JREBEL_USER_PROFILE_INFO_GATHERING);
        ListListStringValueData value = (ListListStringValueData)metric.getValue(context);

        StringBuilder builder = new StringBuilder();
        for (ListStringValueData item : value.getAll()) {
            builder.append(item.toString());
            builder.append('\n');
        }

        return builder;
    }

    private Properties readProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream in = new BufferedInputStream(new FileInputStream(new File(JREBEL_PROPERTIES_RESOURCE)))) {
            properties.load(in);
        }

        return properties;
    }
}
