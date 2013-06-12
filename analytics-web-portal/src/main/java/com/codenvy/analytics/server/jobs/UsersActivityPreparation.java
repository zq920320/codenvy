/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.jobs;

import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.MapStringListValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersActivityPreparation implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersActivityPreparation.class);

    /**
     * @return initialized job
     */
    public static JobDetail createJob() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(UsersActivityPreparation.class.getName()));
        jobDetail.setJobClass(UsersActivityPreparation.class);

        return jobDetail;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("UsersActivityPreparation is started");
        long start = System.currentTimeMillis();

        try {
            ScriptExecutor executor = ScriptExecutor.INSTANCE;
            Map<String, String> executeContext = Utils.initializeContext(TimeUnit.DAY, new Date());

            MapStringListValueData result =
                                            (MapStringListValueData)executor.executeAndReturn(ScriptType.USERS_ACTIVITY_PREPARATION,
                                                                                              executeContext);
            store(result, executeContext);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("UsersActivityPreparation is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void store(MapStringListValueData result, Map<String, String> executeContext) throws IOException {
        Map<String, ListStringValueData> all = result.getAll();

        for (String user : all.keySet()) {
            ListStringValueData item = all.get(user);

            LinkedHashMap<String, String> uuid = new LinkedHashMap<String, String>(3);
            Utils.putFromDate(uuid, Utils.getFromDate(executeContext));
            Utils.putToDate(uuid, Utils.getToDate(executeContext));
            uuid.put(MetricParameter.ALIAS.getName(), user);

            FSValueDataManager.store(item, MetricType.USER_ACTIVITY, uuid);
        }
    }
}
