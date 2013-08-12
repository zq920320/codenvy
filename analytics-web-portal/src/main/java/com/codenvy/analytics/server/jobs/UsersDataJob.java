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

import com.codenvy.analytics.metrics.*;
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
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersDataJob implements Job, ForceableJobRunByContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersDataJob.class);

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Map<String, String> execContext = Utils.initializeContext(TimeUnit.DAY);
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
            prepareDir("PROFILES");

            ScriptExecutor executor = ScriptExecutor.INSTANCE;
            Utils.putResultDir(context, FSValueDataManager.RESULT_DIRECTORY);

            executor.execute(ScriptType.USERS_PROFILE_LOG_PREPARATION, context);

            ValueData result = executor.executeAndReturn(ScriptType.USERS_PROFILE_PREPARATION, context);
            store(MetricType.USER_PROFILE, result, context);

            for (MetricType metricType : MetricType.values()) {
                DataProcessing.calculateAndStore(metricType, context);
            }
        } finally {
            LOGGER.info("UsersDataJob is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void store(MetricType metricType, ValueData valueData, Map<String, String> context)
            throws IOException {
        if (!(valueData instanceof MapStringListListStringValueData)) {
            throw new IOException("MapStringListListStringValueData class is expected");
        }

        MapStringListListStringValueData mapVD = (MapStringListListStringValueData)valueData;
        Map<String, ListListStringValueData> all = mapVD.getAll();

        for (String user : all.keySet()) {
            ListListStringValueData item = all.get(user);

            LinkedHashMap<String, String> uuid = makeUUID(metricType, context, user);
            FSValueDataManager.storeValue(item, metricType, uuid);
        }
    }

    private LinkedHashMap<String, String> makeUUID(MetricType metricType, Map<String, String> executeContext,
                                                   String user) throws IOException {
        LinkedHashMap<String, String> uuid = new LinkedHashMap<>(3);

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

    private void prepareDir(String directory) throws IOException {
        File srcDir = new File(FSValueDataManager.RESULT_DIRECTORY, directory);
        File destDir = new File(FSValueDataManager.RESULT_DIRECTORY, "PREV_" + directory);

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
