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
package com.codenvy.analytics.services.view;

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.services.ConfigurationManager;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.XmlConfigurationManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ViewBuilder implements Feature {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ViewBuilder.class);

    /** The directory where views are stored in. */
    public static final String VIEW_DIR = "view";

    private final ConfigurationManager<ViewConfiguration> configurationManager;

    public ViewBuilder() {
        this.configurationManager = new XmlConfigurationManager<>(ViewConfiguration.class);
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
            doExecute();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            doExecute();
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    protected void doExecute() throws Exception {
        LOG.info("ViewBuilder is started");
        long start = System.currentTimeMillis();

        try {
            String[] views = new File(Configurator.CONFIGURATION_DIRECTORY, VIEW_DIR).list();

            for (String view : views) {
                ViewConfiguration viewConfiguration = configurationManager.loadConfiguration(view);
                build(viewConfiguration);
            }
        } finally {
            LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void build(ViewConfiguration viewConfiguration) throws Exception {
        for (SectionConfiguration sectionConfiguration : viewConfiguration.getSections()) {
            List<List<ValueData>> sectionData = new ArrayList<>(sectionConfiguration.getRows().size());

            for (RowConfiguration rowConfiguration : sectionConfiguration.getRows()) {
                List<ValueData> rowData = new ArrayList<>(sectionConfiguration.getLength() + 1);

                Constructor<?> constructor = Class.forName(rowConfiguration.getClazz()).getConstructor(Map.class);
                Row row = (Row)constructor.newInstance(rowConfiguration.getParamsAsMap());

                Map<String, String> context = Utils.initializeContext(Parameters.TimeUnit.DAY);

                for (int i = 0; i < sectionConfiguration.getLength(); i++) {
                    rowData.add(row.getData(context));
                    context = Utils.nextDateInterval(context);
                }
                rowData.add(row.getDescription());

                Collections.reverse(rowData);
                sectionData.add(rowData);
            }

            retain(sectionData, sectionConfiguration);
        }
    }

    private void retain(List<List<ValueData>> sectionData, SectionConfiguration sectionConfiguration) {
        // TODO preserve
    }
}
