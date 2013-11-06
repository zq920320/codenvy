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
import com.codenvy.analytics.storage.JdbcDataManager;
import com.codenvy.analytics.storage.JdbcDataManagerFactory;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.services.ConfigurationManager;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.XmlConfigurationManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ViewBuilder implements Feature {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ViewBuilder.class);

    /** The directory where views are stored in. */
    public static final String VIEW_DIR = "view";

    private final ConfigurationManager<ViewConfiguration> configurationManager;

    private final JdbcDataManager dataManager;

    public ViewBuilder() {
        this.configurationManager = new XmlConfigurationManager<>(ViewConfiguration.class);
        this.dataManager = JdbcDataManagerFactory.getDataManager();
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
            File[] views = new File(Configurator.CONFIGURATION_DIRECTORY, VIEW_DIR).listFiles();

            for (File view : views != null ? views : new File[0]) {
                ViewConfiguration viewConfiguration = configurationManager.loadConfiguration(view.getAbsolutePath());
                build(viewConfiguration);
            }
        } finally {
            LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void build(ViewConfiguration viewConfiguration) throws Exception {
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);

        for (String timeUnitParam : viewConfiguration.getTimeUnit().split(",")) {
            Parameters.TimeUnit timeUnit = Parameters.TimeUnit.valueOf(timeUnitParam.toUpperCase());

            for (SectionConfiguration sectionConfiguration : viewConfiguration.getSections()) {
                ComputeSectionData task = new ComputeSectionData(sectionConfiguration, timeUnit);
                forkJoinPool.submit(task);
            }
        }

        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    protected void retainData(String tableName, List<List<ValueData>> sectionData) throws SQLException {
        List<ValueData> fields = sectionData.get(0);
        List<List<ValueData>> data = sectionData.subList(1, sectionData.size());

        dataManager.retainData(tableName, fields, data);
    }

    private class ComputeSectionData extends RecursiveAction {

        private final SectionConfiguration sectionConfiguration;

        private final Parameters.TimeUnit timeUnit;

        private ComputeSectionData(SectionConfiguration sectionConfiguration, Parameters.TimeUnit timeUnit) {
            this.sectionConfiguration = sectionConfiguration;
            this.timeUnit = timeUnit;
        }

        @Override
        protected void compute() {
            try {
                List<List<ValueData>> sectionData = new ArrayList<>(sectionConfiguration.getRows().size());

                for (RowConfiguration rowConfiguration : sectionConfiguration.getRows()) {
                    List<ValueData> rowData = new ArrayList<>(sectionConfiguration.getColumns() + 1);

                    Constructor<?> constructor = Class.forName(rowConfiguration.getClazz()).getConstructor(Map.class);
                    Row row = (Row)constructor.newInstance(rowConfiguration.getParamsAsMap());

                    Map<String, String> context = Utils.initializeContext(timeUnit);

                    rowData.add(row.getDescription());
                    for (int i = 0; i < sectionConfiguration.getColumns(); i++) {
                        rowData.add(row.getData(context));
                        context = Utils.prevDateInterval(context);
                    }

                    sectionData.add(rowData);
                }

                String tableName = sectionConfiguration.getName() + "_" + timeUnit.toString().toLowerCase();
                retainData(tableName, sectionData);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
    }
}
