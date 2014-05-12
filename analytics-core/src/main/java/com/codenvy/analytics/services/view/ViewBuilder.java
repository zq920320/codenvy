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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.DataPersister;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Singleton
public class ViewBuilder extends Feature {

    public static final int MAX_CSV_ROWS = 365;

    private static final Logger LOG                 = LoggerFactory.getLogger(ViewBuilder.class);
    private static final String VIEWS_CONFIGURATION = "analytics.views";

    private final DataPersister        jdbcPersister;
    private final CSVReportPersister   csvReportPersister;
    private final DisplayConfiguration displayConfiguration;

    @Inject
    public ViewBuilder(JdbcDataPersisterFactory jdbcDataPersisterFactory,
                       CSVReportPersister csvReportPersister,
                       XmlConfigurationManager confManager,
                       Configurator configurator) throws IOException {
        this.displayConfiguration = new DisplayConfiguration();

        List<ViewConfiguration> views = new ArrayList<>();
        for (String view : configurator.getArray(VIEWS_CONFIGURATION)) {
            DisplayConfiguration dc = confManager.loadConfiguration(DisplayConfiguration.class, view);
            views.addAll(dc.getViews());
        }
        this.displayConfiguration.setViews(views);

        this.jdbcPersister = jdbcDataPersisterFactory.getDataPersister();
        this.csvReportPersister = csvReportPersister;
    }

    public ViewData getViewData(String name, Context context) throws IOException, ParseException {
        ViewConfiguration view = displayConfiguration.getView(name);

        if (!isSimplified(context) || view.isOnDemand()) {
            ComputeViewDataAction computeViewDataAction = new ComputeViewDataAction(view, context);
            return computeViewDataAction.doCompute();
        } else {
            return loadViewData(view, context);
        }
    }

    private boolean isSimplified(Context context) {
        return !context.exists(Parameters.SORT)
               && !context.exists(Parameters.PAGE)
               && !context.exists(Parameters.CSV_ROWS)
               && (!context.exists(Parameters.FROM_DATE) || context.isDefaultValue(Parameters.FROM_DATE))
               && (!context.exists(Parameters.TO_DATE) || context.isDefaultValue(Parameters.TO_DATE))
               && context.getFilters().isEmpty();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected void doExecute(Context context) throws Exception {
        LOG.info("ViewBuilder is started");
        long start = System.currentTimeMillis();

        try {
            computeDisplayData(context);
        } finally {
            LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    public void computeDisplayData(Context context) throws Exception {
        List<RecursiveAction> tasks = new ArrayList<>();

        ForkJoinPool forkJoinPool = new ForkJoinPool();

        for (ViewConfiguration viewConf : displayConfiguration.getViews()) {
            if (!viewConf.isOnDemand()) {
                if (viewConf.getTimeUnit() == null) {
                    ComputeViewDataAction task = new ComputeViewDataAction(viewConf, context);
                    forkJoinPool.submit(task);

                    tasks.add(task);
                } else {
                    for (String timeUnitParam : viewConf.getTimeUnit().split(",")) {
                        Context newContext = context.cloneAndPut(Parameters.TIME_UNIT, timeUnitParam.toUpperCase());
                        ComputeViewDataAction task = new ComputeViewDataAction(viewConf, newContext);
                        forkJoinPool.submit(task);

                        tasks.add(task);
                    }
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
    protected ViewData loadViewData(ViewConfiguration viewConf, Context context)
            throws IOException {
        try {
            ViewData viewData = new ViewData(viewConf.getSections().size());

            for (SectionConfiguration sectionConf : viewConf.getSections()) {
                String sectionId = getId(sectionConf.getName(), context);
                viewData.put(sectionConf.getName(), jdbcPersister.loadData(sectionId));
            }

            return viewData;
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    public void retainViewData(String viewId,
                               ViewData viewData,
                               Context context) throws SQLException, IOException {
        jdbcPersister.storeData(viewData);
        csvReportPersister.storeData(viewId, viewData, context);
    }

    private class ComputeViewDataAction extends RecursiveAction {

        private final ViewConfiguration viewConf;
        private final Context           context;

        private ComputeViewDataAction(ViewConfiguration viewConfiguration,
                                      Context context) throws ParseException {
            this.viewConf = viewConfiguration;
            this.context = initializeFirstInterval(context);
        }

        @Override
        protected void compute() {
            String viewId = getId(viewConf.getName(), context);

            try {
                ViewData viewData = doCompute();
                retainViewData(viewId, viewData, context);
            } catch (Throwable e) {
                String message = "Can't compute view " + viewId;
                LOG.error(message, e);
            }
        }

        private ViewData doCompute() throws IOException {
            LOG.info("ViewBuilder is started for " + viewConf.getName());
            long start = System.currentTimeMillis();

            try {
                ViewData viewData = new ViewData(viewConf.getSections().size());

                for (SectionConfiguration sectionConf : viewConf.getSections()) {
                    SectionData sectionData = new SectionData(sectionConf.getRows().size());

                    for (RowConfiguration rowConf : sectionConf.getRows()) {
                        Row row = getRow(rowConf);

                        int rowCount = getRowCount(viewConf.getColumns(), context);
                        sectionData.addAll(row.getData(context, rowCount));
                    }

                    String sectionId = getId(sectionConf.getName(), context);
                    viewData.put(sectionId, sectionData);
                }

                return viewData;
            } catch (NoSuchMethodException | ClassCastException | ClassNotFoundException | InvocationTargetException |
                    IllegalAccessException | InstantiationException e) {
                throw new IOException(e);
            } finally {
                LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec. for " +
                         viewConf.getName() + " with context " + context);
            }
        }

        private Row getRow(RowConfiguration rowConf)
                throws NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException,
                       InvocationTargetException {
            String className = rowConf.getClazz();

            if (context.exists(Parameters.CSV_ROWS)) {
                if (DateRow.class.getName().equals(className)) {
                    className = CVSDateRow.class.getName();
                }
            }

            Constructor<?> constructor = Class.forName(className).getConstructor(Map.class);
            return (Row)constructor.newInstance(rowConf.getParamsAsMap());
        }
    }

    private Context initializeFirstInterval(Context context) throws ParseException {
        Context.Builder builder = new Context.Builder(context);
        builder.put(Parameters.REPORT_DATE, builder.getAsString(Parameters.TO_DATE));

        if (context.exists(Parameters.TIME_UNIT)) {
            Parameters.TimeUnit timeUnit = builder.getTimeUnit();
            if (context.exists(Parameters.TIME_INTERVAL)) {
                int timeShift = (int)-context.getAsLong(Parameters.TIME_INTERVAL);
                return Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), timeUnit, timeShift, builder);
            } else {
                return Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), timeUnit, builder);
            }
        } else {
            return builder.build();
        }
    }

    private int getRowCount(int rowCountFromConf, Context context) {
        if (context.exists(Parameters.CSV_ROWS)) {
            return (int)context.getAsLong(Parameters.CSV_ROWS);
        } else if (context.exists(Parameters.TIME_UNIT) && context.getTimeUnit() == Parameters.TimeUnit.LIFETIME) {
            return 2;
        } else {
            return rowCountFromConf;
        }
    }

    private String getId(String idFromConf, Context context) {
        String id = idFromConf;
        if (context.exists(Parameters.TIME_UNIT)) {
            id += "_" + context.getAsString(Parameters.TIME_UNIT).toLowerCase();
        }

        return id;
    }
}
