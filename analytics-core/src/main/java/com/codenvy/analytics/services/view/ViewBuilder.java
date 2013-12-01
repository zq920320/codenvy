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

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.ConfigurationManager;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.XmlConfigurationManager;
import com.codenvy.analytics.storage.CSVDataPersister;
import com.codenvy.analytics.storage.DataPersister;
import com.codenvy.analytics.storage.JdbcDataPersisterFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private final DataPersister                              dataPersister;
    private final CSVDataPersister                           csvDataPersister;
    private final ConfigurationManager<DisplayConfiguration> configurationManager;

    public ViewBuilder() {
        this.configurationManager = new XmlConfigurationManager<>(DisplayConfiguration.class);
        this.dataPersister = JdbcDataPersisterFactory.getDataManager();
        this.csvDataPersister = new CSVDataPersister();
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
            DisplayConfiguration displayConfiguration = configurationManager.loadConfiguration("views.xml");
            build(displayConfiguration);
        } finally {
            LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void build(DisplayConfiguration displayConfiguration) throws Exception {
        List<RecursiveAction> tasks = new ArrayList<>();

        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 2);

        for (ViewConfiguration viewConfiguration : displayConfiguration.getViews()) {
            for (String timeUnitParam : viewConfiguration.getTimeUnit().split(",")) {
                Parameters.TimeUnit timeUnit = Parameters.TimeUnit.valueOf(timeUnitParam.toUpperCase());

                for (SectionConfiguration sectionConfiguration : viewConfiguration.getSections()) {
                    ComputeSectionData task = new ComputeSectionData(sectionConfiguration, timeUnit);
                    forkJoinPool.submit(task);

                    tasks.add(task);
                }
            }
        }

        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        for (RecursiveAction task : tasks) {
            if (!task.isDone()) {
                throw new IllegalStateException("Task wasn't done");
            } else if (task.getException() != null) {
                throw new IllegalStateException(task.getException());
            }
        }
    }

    protected void retainData(String tableName,
                              List<List<ValueData>> sectionData,
                              Map<String, String> context) throws SQLException, IOException {

        List<ValueData> fields = sectionData.get(0);
        List<List<ValueData>> data = sectionData.subList(1, sectionData.size());

        dataPersister.retainData(tableName, fields, data, context);
        csvDataPersister.retainData(tableName, fields, data, context);
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
                int rowCount = timeUnit == Parameters.TimeUnit.LIFETIME ? 1 : sectionConfiguration.getColumns();
                List<List<ValueData>> sectionData = new ArrayList<>(sectionConfiguration.getRows().size());

                for (RowConfiguration rowConfiguration : sectionConfiguration.getRows()) {
                    Map<String, String> context = Utils.initializeContext(timeUnit);
                    List<ValueData> rowData = new ArrayList<>(sectionConfiguration.getColumns() + 1);

                    Constructor<?> constructor = Class.forName(rowConfiguration.getClazz()).getConstructor(Map.class);
                    Row row = (Row)constructor.newInstance(rowConfiguration.getParamsAsMap());

                    rowData.add(row.getDescription());
                    for (int i = 0; i < rowCount; i++) {
                        rowData.add(row.getData(context));
                        context = Utils.prevDateInterval(context);
                    }

                    sectionData.add(rowData);
                }

                String tableName = sectionConfiguration.getName() + "_" + timeUnit.toString().toLowerCase();
                retainData(tableName, sectionData, Utils.initializeContext(Parameters.TimeUnit.DAY));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
    }
}
