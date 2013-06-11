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
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersProfilePreparation implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersProfilePreparation.class);

    /**
     * @return initialized job
     */
    public static JobDetail createJob() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(UsersProfilePreparation.class.getName()));
        jobDetail.setJobClass(UsersProfilePreparation.class);

        return jobDetail;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("UsersProfilePreparation is started");
        long start = System.currentTimeMillis();

        try {
            ScriptExecutor executor = ScriptExecutor.INSTANCE;
            Map<String, String> executeContext = Utils.initializeContext(TimeUnit.DAY, new Date());
            
            ListListStringValueData result =
                                             (ListListStringValueData)executor.executeAndReturn(ScriptType.USERS_PROFILE_PREPARATION,
                                                                                                executeContext);

            store(result);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            LOGGER.info("UsersProfilePreparation is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    private void store(ListListStringValueData result) throws IOException {
        for (ListStringValueData item : result.getAll()) {
            Map<String, String> uuid = new HashMap<>();
            uuid.put(MetricParameter.ALIAS.getName(), item.getAll().get(0));
            
            FSValueDataManager.store(item, MetricType.USER_PROFILE, uuid);
        }
    }
}
