/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import org.apache.commons.io.FileUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersDataJob implements Job, ForceableJobRunByContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersDataJob.class);

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, String> execContext = Utils.initializeContext(TimeUnit.DAY, new Date());
            run(execContext);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forceRun(Map<String, String> context) throws Exception {
        run(context);
    }

    private void run(Map<String, String> context) throws Exception {
        LOGGER.info("UsersDataJob is started " + context.toString());
        long start = System.currentTimeMillis();

        try {
            prepareDirs();

            ScriptExecutor executor = ScriptExecutor.INSTANCE;
            Utils.putResultDir(context, FSValueDataManager.RESULT_DIRECTORY);

            ValueData result = executor.executeAndReturn(ScriptType.USERS_ACTIVITY_PREPARATION, context);
            store(MetricType.USER_ACTIVITY, result, context);

            executor.execute(ScriptType.USERS_PROFILE_LOG_PREPARATION, context);

            result = executor.executeAndReturn(ScriptType.USERS_SESSIONS_PREPARATION, context);
            store(MetricType.USER_SESSIONS, result, context);

            result = executor.executeAndReturn(ScriptType.USERS_PROFILE_PREPARATION, context);
            store(MetricType.USER_PROFILE, result, context);

            MetricFactory.createMetric(MetricType.PROJECTS_BUILT_NUMBER).getValue(context);
            MetricFactory.createMetric(MetricType.PROJECTS_DEPLOYED_NUMBER).getValue(context);
        } finally {
            LOGGER.info("UsersDataJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void store(MetricType metricType, ValueData valueData, Map<String, String> executeContext) throws IOException {
        if (!(valueData instanceof MapStringListListStringValueData)) {
            throw new IOException("MapStringListListStringValueData class is expected");
        }

        MapStringListListStringValueData mapVD = (MapStringListListStringValueData)valueData;
        Map<String, ListListStringValueData> all = mapVD.getAll();

        for (String user : all.keySet()) {
            ListListStringValueData item = all.get(user);

            LinkedHashMap<String, String> uuid = makeUUID(metricType, executeContext, user);
            FSValueDataManager.store(item, metricType, uuid);
        }
    }

    private LinkedHashMap<String, String> makeUUID(MetricType metricType, Map<String, String> executeContext, String user) throws IOException {
        LinkedHashMap<String, String> uuid = new LinkedHashMap<String, String>(3);

        Metric metric = MetricFactory.createMetric(metricType);
        for (MetricParameter param : metric.getParams()) {
            switch (param) {
                case FROM_DATE:
                    Utils.putFromDate(uuid, Utils.getFromDate(executeContext));
                    break;
                case TO_DATE:
                    Utils.putToDate(uuid, Utils.getToDate(executeContext));
                    break;
                case ALIAS:
                    uuid.put(MetricParameter.ALIAS.name(), user);
                    break;
                default:
                    throw new IllegalStateException("Metric parameter " + param + " is not supported");
            }
        }

        return uuid;
    }

    private void prepareDirs() throws IOException {
        File srcDir = new File(FSValueDataManager.RESULT_DIRECTORY, "PROFILES");
        File destDir = new File(FSValueDataManager.RESULT_DIRECTORY, "PREV_PROFILES");

        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }

        if (srcDir.exists()) {
            FileUtils.moveDirectory(srcDir, destDir);
        } else {
            destDir.mkdirs();
            File.createTempFile("prefix", "suffix", destDir);
        }
    }
}
