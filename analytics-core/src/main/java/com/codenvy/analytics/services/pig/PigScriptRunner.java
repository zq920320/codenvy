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
import com.codenvy.analytics.services.pig.impl.PigScriptRunnerConfigurationException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class PigScriptRunner implements Feature {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(PigScriptRunner.class);

    /** Runtime parameter name. Contains the configuration of PigExecutorService. */
    public static final String ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY = "analytics.pig.runner.config";

    /** The value of {@value #ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY} runtime parameter. */
    public static final String PIG_EXECUTOR_CONFIG = System.getProperty(ANALYTICS_PIG_EXECUTOR_CONFIG_PROPERTY);

    private Scheduler scheduler;

    /**
     * The private constructor.
     *
     * @throws com.codenvy.analytics.services.pig.impl.PigScriptRunnerConfigurationException
     *
     * @throws ShedulingExecutionEntryException
     *
     */
    public PigScriptRunner() throws PigScriptRunnerConfigurationException, ShedulingExecutionEntryException {

//        LOG.info("PigScriptRunner was started with configuration: " + PIG_EXECUTOR_CONFIG);
//        LOG.info("PigScriptRunner was scheduled tasks :\n" + configuration.toString());
    }

    protected PigScriptRunnerConfiguration readConfiguration() throws PigScriptRunnerConfigurationException {
        try (InputStream in = openConfigurationResource()) {
            IBindingFactory factory = BindingDirectory.getFactory(PigScriptRunnerConfiguration.class);
            IUnmarshallingContext uctx = factory.createUnmarshallingContext();

            PigScriptRunnerConfiguration configuration =
                    (PigScriptRunnerConfiguration)uctx.unmarshalDocument(new InputStreamReader(in));

            return configuration;
        } catch (JiBXException | IOException e) {
            throw new PigScriptRunnerConfigurationException("Can not read the configuration of PigExecutorService: "
                                                            + PIG_EXECUTOR_CONFIG, e);
        }
    }

    protected InputStream openConfigurationResource() throws PigScriptRunnerConfigurationException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(PIG_EXECUTOR_CONFIG);

        if (in == null) {
            throw new PigScriptRunnerConfigurationException("Resource " + PIG_EXECUTOR_CONFIG + " not found");
        }

        return in;
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
