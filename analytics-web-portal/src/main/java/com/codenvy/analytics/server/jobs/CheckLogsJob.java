/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;
import com.codenvy.analytics.server.service.MailService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CheckLogsJob implements Job, ForceableJobRunByContext {

    private static final Logger LOGGER                        = LoggerFactory.getLogger(CheckLogsJob.class);
    private static final String CHECKLOGS_PROPERTIES_RESOURCE =
            System.getProperty("analytics.job.checklogs.properties");

    private final Properties checkLogsProperties;

    public CheckLogsJob() throws IOException {
        checkLogsProperties = Utils.readProperties(CHECKLOGS_PROPERTIES_RESOURCE);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, String> executionContext = Utils.initializeContext(TimeUnit.DAY);
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
        LOGGER.info("CheckLogsJob is started");
        long start = System.currentTimeMillis();

        try {
            String date = Utils.getToDateParam(context);

            ValueData valueData = ScriptExecutor.INSTANCE.executeAndReturn(ScriptType.CHECK_LOGS_1, context);
            valueData = valueData.union(ScriptExecutor.INSTANCE.executeAndReturn(ScriptType.CHECK_LOGS_2, context));

            sendMail((ListListStringValueData)valueData, date);
        } finally {
            LOGGER.info("CheckLogsJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void sendMail(ListListStringValueData valueData, String date) throws IOException {
        MailService mailService = new MailService(checkLogsProperties);

        StringBuilder builder = new StringBuilder();
        for (ListStringValueData item : valueData.getAll()) {
            builder.append(item.getAsString());
            builder.append('\n');
        }

        mailService.setSubject("Log checking for " + date);
        mailService.setText(builder.toString());

        mailService.send();
    }
}
