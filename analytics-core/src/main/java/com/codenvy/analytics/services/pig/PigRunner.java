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

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.services.ConfigurationManager;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.XmlConfigurationManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class PigRunner implements Feature {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(PigRunner.class);

    private final ConfigurationManager<PigRunnerConfiguration> configurationManager;

    public PigRunner() {
        this.configurationManager = new XmlConfigurationManager<>(PigRunnerConfiguration.class);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAvailable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void forceExecute(Map<String, String> context) throws JobExecutionException {
        try {
            doExecute(context);
        } catch (IOException e) {
            throw new JobExecutionException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            doExecute(Utils.initializeContext(Parameters.TimeUnit.DAY));
        } catch (IOException | ParseException e) {
            throw new JobExecutionException(e);
        }
    }

    protected void doExecute(Map<String, String> context) throws IOException {
        LOG.info("PigRunner is started");
        long start = System.currentTimeMillis();

        try {
            String resource = Configurator.ANALYTICS_CONF_DIR + File.separator + "running-scripts.xml";
            PigRunnerConfiguration configuration = configurationManager.loadConfiguration(resource);

            for (ScriptConfiguration scriptConfiguration : configuration.getScripts()) {
                String scriptName = scriptConfiguration.getName();
                Map<String, String> parameters = scriptConfiguration.getParameters();

                ScriptType scriptType = ScriptType.valueOf(scriptName.toUpperCase());
                parameters.putAll(context);

                PigServer.executeOnServer(scriptType, parameters);
            }
        } finally {
            LOG.info("PigRunner is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }
}
