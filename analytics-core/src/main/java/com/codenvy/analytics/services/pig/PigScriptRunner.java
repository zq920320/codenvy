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
package com.codenvy.analytics.services.pig;

import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.pig.config.PigScriptRunnerConfiguration;
import com.codenvy.analytics.services.pig.config.ScriptEntry;
import com.codenvy.analytics.services.pig.impl.PigExecutorServiceConfigurationException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class PigScriptRunner implements PigExecutorService, Feature {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(PigScriptRunner.class);

    private PigScriptRunnerConfiguration configuration;

    private Scheduler scheduler;

    /**
     * The private constructor.
     *
     * @throws com.codenvy.analytics.services.pig.impl.PigExecutorServiceConfigurationException
     *
     * @throws ShedulingExecutionEntryException
     *
     */
    public PigScriptRunner() throws PigExecutorServiceConfigurationException, ShedulingExecutionEntryException {
        try {
            if (PIG_EXECUTOR_CONFIG == null)
                throw new PigExecutorServiceConfigurationException("The system property with configuration not found: "
                                                                   + ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY);

            URL configURL = getClass().getClassLoader().getResource(PIG_EXECUTOR_CONFIG);

            if (configURL == null)
                throw new PigExecutorServiceConfigurationException(
                        "Can not found the configuration of PigExecutorService: "
                        + PIG_EXECUTOR_CONFIG);

            IBindingFactory factory = BindingDirectory.getFactory(PigScriptRunnerConfiguration.class);
            IUnmarshallingContext uctx = factory.createUnmarshallingContext();

            configuration =
                    (PigScriptRunnerConfiguration)uctx
                            .unmarshalDocument(new FileInputStream(new File(configURL.getPath())),
                                               null);

        } catch (JiBXException e) {
            throw new PigExecutorServiceConfigurationException("Can not read the configuration of PigExecutorService: "
                                                               + PIG_EXECUTOR_CONFIG, e);
        } catch (FileNotFoundException e) {
            throw new PigExecutorServiceConfigurationException("Can not found the configuration of PigExecutorService: "
                                                               + PIG_EXECUTOR_CONFIG, e);
        }

        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            this.scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            LOG.error("Can not get Quartz sheduler : ", e);
            throw new PigExecutorServiceConfigurationException("Can not get Quartz sheduler: ", e);
        }


        for (ExecutionEntry executionEntry : configuration.getScripts()) {
            this.schedule(executionEntry);
        }

        try {
            scheduler.start();
        } catch (SchedulerException e) {
            throw new PigExecutorServiceConfigurationException("Can not start Quartz sheduler.", e);
        }

        LOG.info("PigScriptRunner was started with configuration: " + PIG_EXECUTOR_CONFIG);
        LOG.info("PigScriptRunner was scheduled tasks :\n" + configuration.toString());
    }

    protected PigScriptRunnerConfiguration readConfiguration() {
        return null;
    }

    @Override
    public void schedule(ExecutionEntry executionEntry) throws ShedulingExecutionEntryException {

    }

    /** {@inheritDoc} */
    @Override
    public void execute(ScriptEntry scriptEntry) {
        //TODO  will be executed pig script, thanks PigScriptExecutor
//        PigServer.execute(null, null);

        LOG.info("Executing script:" + scriptEntry.toString());
    }

    @Override
    public ExecutionEntry getScheduledTask(String key) throws ShedulingExecutionEntryException {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PigScriptRunnerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void shutdown() {

    }

    /** {@inheritDoc} */
    @Override
    public boolean isAvailable() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void forceRun(Map<String, String> context) {

    }

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

    }
}
