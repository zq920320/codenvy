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

    private static final Logger LOG                   = LoggerFactory.getLogger(ViewBuilder.class);
    public static final  String VIEW_CONFIGS_PROPERTY = "view.configs";

    private final DataPersister        jdbcPersister;
    private final CSVReportPersister   csvReportPersister;
    private final DisplayConfiguration displayConfiguration;

    @Inject
    public ViewBuilder(JdbcDataPersisterFactory jdbcDataPersisterFactory,
                       CSVReportPersister csvReportPersister,
                       XmlConfigurationManager confManager,
                       Configurator configurator) throws IOException {
        this.displayConfiguration = new DisplayConfiguration();

        List<ViewConfiguration> views = new ArrayList<ViewConfiguration>();
        for (String view : configurator.getArray(VIEW_CONFIGS_PROPERTY)) {
            DisplayConfiguration dc = confManager.loadConfiguration(DisplayConfiguration.class, view);
            views.addAll(dc.getViews());
        }
        this.displayConfiguration.setViews(views);

        this.jdbcPersister = jdbcDataPersisterFactory.getDataPersister();
        this.csvReportPersister = csvReportPersister;
    }

    public ViewData getViewData(String name, Map<String, String> context) throws IOException, ParseException {
        ViewConfiguration view = displayConfiguration.getView(name);

        if (!view.isOnDemand() && Utils.isSimpleContext(context)) {
            return queryViewData(view, context);
        } else {
            ComputeViewDataAction computeViewDataAction = new ComputeViewDataAction(view, context);
            return computeViewDataAction.doCompute();
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected void putParametersInContext(Map<String, String> context) {
    }

    @Override
    protected void doExecute(Map<String, String> context) throws Exception {
        LOG.info("ViewBuilder is started");
        long start = System.currentTimeMillis();

        try {
            computeDisplayData(context);
        } finally {
            LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        }
    }

    protected void computeDisplayData(Map<String, String> context) throws Exception {
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
                        Map<String, String> newContext =
                                Parameters.TIME_UNIT.cloneAndPut(context, timeUnitParam.toUpperCase());

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
    protected ViewData queryViewData(ViewConfiguration viewConf, Map<String, String> context)
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

    protected void retainViewData(String viewId,
                                  ViewData viewData,
                                  Map<String, String> context) throws SQLException, IOException {
        jdbcPersister.storeData(viewData);
        csvReportPersister.storeData(viewId, viewData, context);
    }

    private class ComputeViewDataAction extends RecursiveAction {

        private final ViewConfiguration   viewConfiguration;
        private final Map<String, String> context;

        private ComputeViewDataAction(ViewConfiguration viewConfiguration,
                                      Map<String, String> context) throws ParseException {
            this.viewConfiguration = viewConfiguration;
            this.context = initializeFirstInterval(context);
        }

        public ViewData doCompute() throws IOException {
            return doCompute(viewConfiguration, context);
        }

        @Override
        protected void compute() {
            String viewId = getId(viewConfiguration.getName(), context);

            try {
                ViewData viewData = doCompute(viewConfiguration, context);
                retainViewData(viewId, viewData, context);
            } catch (Throwable e) {
                String message = "Can't compute view " + viewId;
                LOG.error(message, e);
                throw new IllegalStateException(message, e);
            }
        }

        private ViewData doCompute(ViewConfiguration viewConf, Map<String, String> context) throws IOException {
            try {
                ViewData viewData = new ViewData(viewConf.getSections().size());

                for (SectionConfiguration sectionConf : viewConf.getSections()) {
                    SectionData sectionData = new SectionData(sectionConf.getRows().size());

                    for (RowConfiguration rowConf : sectionConf.getRows()) {
                        Constructor<?> constructor = Class.forName(rowConf.getClazz()).getConstructor(Map.class);
                        Row row = (Row)constructor.newInstance(rowConf.getParamsAsMap());

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
            }
        }
    }

    private Map<String, String> initializeFirstInterval(Map<String, String> context) throws ParseException {
        if (!Parameters.TO_DATE.exists(context)) {
            context = Utils.clone(context);
            Parameters.TO_DATE.putDefaultValue(context);
            Parameters.FROM_DATE.put(context, Parameters.TO_DATE.get(context));
            Parameters.REPORT_DATE.put(context, Parameters.TO_DATE.get(context));
            return context;

        } else if (Parameters.TIME_UNIT.exists(context)) {
            context = Utils.clone(context);
            Parameters.REPORT_DATE.put(context, Parameters.TO_DATE.get(context));
            Utils.initDateInterval(Utils.getToDate(context), context);
            return context;


        } else {
            return Parameters.REPORT_DATE.cloneAndPut(context, Parameters.TO_DATE.get(context));
        }
    }

    private int getRowCount(int rowCountFromConf, Map<String, String> context) {
        if (Parameters.TIME_UNIT.exists(context) && Utils.getTimeUnit(context) == Parameters.TimeUnit.LIFETIME) {
            return 2;
        } else {
            return rowCountFromConf;
        }
    }

    private String getId(String idFromConf, Map<String, String> context) {
        String id = idFromConf;
        if (Parameters.TIME_UNIT.exists(context)) {
            id += "_" + Parameters.TIME_UNIT.get(context).toLowerCase();
        }

        return id;
    }
}
