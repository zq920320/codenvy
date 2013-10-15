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
package com.codenvy.analytics.modules.pigexecutor.impl;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codenvy.analytics.modules.pigexecutor.PigExecutorService;
import com.codenvy.analytics.modules.pigexecutor.ShedulingExecutionEntryException;
import com.codenvy.analytics.modules.pigexecutor.config.ExecutionEntry;
import com.codenvy.analytics.modules.pigexecutor.config.PigScriptsExecutorConfiguration;
import com.codenvy.analytics.modules.pigexecutor.config.ScriptEntry;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class PigExecutorServiceImpl implements PigExecutorService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(PigExecutorServiceImpl.class);
    
    private static PigExecutorServiceImpl instance = null;
    
    private AtomicLong numberOfScriptsExecuted = new AtomicLong(0);

    private PigScriptsExecutorConfiguration configuration;

    private Scheduler scheduler;

    private HashMap<String, ExecutionEntry> executionEntryMap;

    /**
     * Get the singleton instance of PigExecutorServiceImpls
     * 
     * @return PigExecutorServiceImpl
     *            return singleton instance of PigExecutorServiceImpls.
     */
    public static PigExecutorServiceImpl getInstance() {
        if (instance == null) {
            try {
                instance = new PigExecutorServiceImpl();
            } catch (PigExecutorServiceConfigurationException e) {
                LOG.error("Can not instantiate " + PigExecutorServiceImpl.class, e);
                throw new RuntimeException("Can not instantiate " + PigExecutorServiceImpl.class, e);
            } catch (ShedulingExecutionEntryException e) {
                LOG.error("Can not instantiate, do not shchedule execution task " + PigExecutorServiceImpl.class, e);
                throw new RuntimeException("Can not instantiate, do not shchedule execution task " + PigExecutorServiceImpl.class, e);
            }
        }
        return instance;
    }

    /**
     * The private constructor.
     * 
     * @throws PigExecutorServiceConfigurationException
     * @throws ShedulingExecutionEntryException
     */
    private PigExecutorServiceImpl() throws PigExecutorServiceConfigurationException, ShedulingExecutionEntryException
    {
        try {
            if (PIG_EXECUTOR_CONFIG == null)
                throw new PigExecutorServiceConfigurationException("The system property with configuration not congigured :"
                                                                   + ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY);
            
            URL configURL = getClass().getClassLoader().getResource(PIG_EXECUTOR_CONFIG);
            
            if (configURL == null)
                throw new PigExecutorServiceConfigurationException("Can not found the configuration of PigExecutorService :"
                    + PIG_EXECUTOR_CONFIG);
            
            IBindingFactory factory = BindingDirectory.getFactory(PigScriptsExecutorConfiguration.class);
            IUnmarshallingContext uctx = factory.createUnmarshallingContext();

            configuration =
                            (PigScriptsExecutorConfiguration)uctx.unmarshalDocument(new FileInputStream(new File(configURL.getPath())),
                                                                                    null);

        } catch (JiBXException e) {
            throw new PigExecutorServiceConfigurationException("Can not read the configuration of PigExecutorService :"
                                                               + PIG_EXECUTOR_CONFIG, e);
        } catch (FileNotFoundException e) {
            throw new PigExecutorServiceConfigurationException("Can not found the configuration of PigExecutorService :"
                                                               + PIG_EXECUTOR_CONFIG, e);
        }

        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            this.scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            LOG.error("Can not get Quartz sheduler : ", e);
            throw new PigExecutorServiceConfigurationException("Can not get Quartz sheduler : ", e);
        }

        this.executionEntryMap = new LinkedHashMap<String, ExecutionEntry>();


        for (ExecutionEntry executionEntry : configuration.getExecutions()) {
            this.schedule(executionEntry);
        }

        try {
            scheduler.start();
        } catch (SchedulerException e) {
            throw new PigExecutorServiceConfigurationException("Can not start Quartz sheduler.", e);
        }

        LOG.info("PigExecutorServiceImpl was started with configuration : " + PIG_EXECUTOR_CONFIG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void schedule(ExecutionEntry executionEntry) throws ShedulingExecutionEntryException {

        String jobName = "JOB_NAME_" + executionEntry.toString().hashCode();

        String jobGroup = "JOB_GROUP_" + executionEntry.toString().hashCode();

        String triggerName = "TRIGGER_NAME_" + executionEntry.toString().hashCode();


        JobDetail job = newJob(ExecutionTask.class)
                                                   .withIdentity(jobName, jobGroup)
                                                   .build();


        CronTrigger trigger = newTrigger()
                                          .withIdentity(triggerName, jobGroup)
                                          .withSchedule(cronSchedule(executionEntry.getSchedule()))
                                          .build();

        try {
            Date ft = scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new ShedulingExecutionEntryException("Can not schedule task for ExecutionEntry : " + executionEntry, e);
        }

        String key = job.getKey().toString() + "_" + trigger.getKey().toString();
        executionEntryMap.put(key, executionEntry);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ScriptEntry scriptEntry) {

        numberOfScriptsExecuted.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionEntry getScheduledTask(String key) throws ShedulingExecutionEntryException {
        if (executionEntryMap.containsKey(key))
            return executionEntryMap.get(key);
        else
            throw new ShedulingExecutionEntryException("PigExecutorServiceImpl has not ExecutionEntry with KEY = " + key);
    }

    /*
     * TODO START STOP
     */
    public void shutdown() {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            throw new RuntimeException("Can not shutdown tha all task", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PigScriptsExecutorConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get the number of scripts executed.
     * 
     * @return long
     */
    public long getNumberOfScriptsExecuted() {
        return numberOfScriptsExecuted.get();
    }
}
