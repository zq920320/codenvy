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
import com.codenvy.analytics.persistent.DataPersister;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.configuration.ConfigurationManager;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/** @author <a href="mailto:areshetnyak@codenvy.com">Alexander Reshetnyak</a> */
public class ViewBuilder extends Feature {

    private static final Logger LOG           = LoggerFactory.getLogger(ViewBuilder.class);
    private static final String CONFIGURATION = "views.xml";

    private final DataPersister                              jdbcPersister;
    private final ConfigurationManager<DisplayConfiguration> configurationManager;
    private final CSVReportPersister                         csvReportPersister;
    private final ForkJoinPool                               forkJoinPool;

    public ViewBuilder() {
        this.configurationManager = new XmlConfigurationManager<>(DisplayConfiguration.class, CONFIGURATION);
        this.jdbcPersister = JdbcDataPersisterFactory.getDataPersister();
        this.csvReportPersister = new CSVReportPersister();
        this.forkJoinPool = new ForkJoinPool();
    }

    public Map<String, List<List<ValueData>>> getViewData(String name, Map<String, String> context) throws IOException {
        DisplayConfiguration displayConfiguration = configurationManager.loadConfiguration();
        ViewConfiguration view = displayConfiguration.getView(name);

        if (Utils.isSimpleContext(context)) {
            return queryViewData(view, context);
        } else {
            return computeViewData(view, context);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected Map<String, String> initializeDefaultContext() throws ParseException {
        return Utils.initializeContext(Parameters.TimeUnit.DAY);
    }

    @Override
    protected void putParametersInContext(Map<String, String> context) {
    }

    @Override
    protected void doExecute(Map<String, String> context) throws Exception {
        LOG.info("ViewBuilder is started");
        long start = System.currentTimeMillis();

        try {
            computeDisplayData(configurationManager.loadConfiguration());
        } finally {
            LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void computeDisplayData(DisplayConfiguration displayConfiguration) throws Exception {
        List<RecursiveAction> tasks = new ArrayList<>();

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        for (ViewConfiguration viewConfiguration : displayConfiguration.getViews()) {
            if (!viewConfiguration.isOnDemand()) {
                for (String timeUnitParam : viewConfiguration.getTimeUnit().split(",")) {
                    Parameters.TimeUnit timeUnit = Parameters.TimeUnit.valueOf(timeUnitParam.toUpperCase());

                    ComputeViewDataAction task = new ComputeViewDataAction(viewConfiguration, timeUnit);
                    forkJoinPool.submit(task);

                    tasks.add(task);
                }
            }
        }

        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        for (RecursiveAction task : tasks) {
            if (task.getException() != null) {
                throw new IllegalStateException(task.getException());
            } else if (!task.isDone()) {
                throw new IllegalStateException("Task wasn't done");

            }
        }
    }

    /** Query data for specific view. */
    protected Map<String, List<List<ValueData>>> queryViewData(ViewConfiguration viewConfiguration,
                                                               Map<String, String> context) throws IOException {
        try {
            Map<String, List<List<ValueData>>> viewData = new LinkedHashMap<>(viewConfiguration.getSections().size());
            Parameters.TimeUnit timeUnit = Utils.getTimeUnit(context);

            for (SectionConfiguration sectionConfiguration : viewConfiguration.getSections()) {
                String sectionId = sectionConfiguration.getName() + "_" + timeUnit.toString().toLowerCase();
                viewData.put(sectionConfiguration.getName(), jdbcPersister.loadData(sectionId));
            }

            return viewData;
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /** Compute data for specific view. */
    protected Map<String, List<List<ValueData>>> computeViewData(ViewConfiguration viewConfiguration,
                                                                 Map<String, String> context) throws IOException {
        try {
            Map<String, List<List<ValueData>>> viewData = new LinkedHashMap<>(viewConfiguration.getSections().size());
            Parameters.TimeUnit timeUnit = Utils.getTimeUnit(context);

            for (SectionConfiguration sectionConfiguration : viewConfiguration.getSections()) {

                List<List<ValueData>> sectionData = new ArrayList<>(sectionConfiguration.getRows().size());

                for (RowConfiguration rowConfiguration : sectionConfiguration.getRows()) {
                    Constructor<?> constructor = Class.forName(rowConfiguration.getClazz()).getConstructor(Map.class);
                    Row row = (Row)constructor.newInstance(rowConfiguration.getParamsAsMap());

                    int rowCount = timeUnit == Parameters.TimeUnit.LIFETIME ? 2 : viewConfiguration.getColumns();
                    sectionData.addAll(row.getData(context, rowCount));
                }

                String sectionId = sectionConfiguration.getName() + "_" + timeUnit.toString().toLowerCase();
                viewData.put(sectionId, sectionData);
            }

            return viewData;
        } catch (NoSuchMethodException | ClassCastException | ClassNotFoundException | InvocationTargetException |
                IllegalAccessException | InstantiationException e) {
            throw new IOException(e);
        }
    }

    protected void retainViewData(String viewId,
                                  Map<String, List<List<ValueData>>> viewData,
                                  Map<String, String> context) throws SQLException, IOException {
        jdbcPersister.storeData(viewData, context);
        csvReportPersister.storeData(viewId, viewData, context);
    }

    private class ComputeViewDataAction extends RecursiveAction {

        private final ViewConfiguration   viewConfiguration;
        private final Parameters.TimeUnit timeUnit;

        private ComputeViewDataAction(ViewConfiguration viewConfiguration, Parameters.TimeUnit timeUnit) {
            this.viewConfiguration = viewConfiguration;
            this.timeUnit = timeUnit;
        }

        @Override
        protected void compute() {
            try {
                String viewId = viewConfiguration.getName() + "_" + timeUnit.toString().toLowerCase();
                Map<String, String> context = Utils.initializeContext(timeUnit);

                Map<String, List<List<ValueData>>> viewData = computeViewData(viewConfiguration, context);

                retainViewData(viewId, viewData, Utils.initializeContext(Parameters.TimeUnit.DAY));
            } catch (IOException | ParseException | SQLException e) {
                LOG.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
    }
}
