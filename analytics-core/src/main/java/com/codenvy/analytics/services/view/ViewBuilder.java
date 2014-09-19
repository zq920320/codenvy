/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
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
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.sessions.AbstractTimelineProductUsageCondition;
import com.codenvy.analytics.metrics.top.AbstractTopEntitiesTime;
import com.codenvy.analytics.persistent.DataPersister;
import com.codenvy.analytics.persistent.JdbcDataPersisterFactory;
import com.codenvy.analytics.services.Feature;
import com.codenvy.analytics.services.configuration.XmlConfigurationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.codenvy.analytics.Utils.initDateInterval;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;


/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Singleton
public class ViewBuilder extends Feature {

    public static final int MAX_ROWS = 365;

    private static final Logger LOG                 = LoggerFactory.getLogger(ViewBuilder.class);
    private static final String VIEWS_CONFIGURATION = "analytics.views";

    private final DataPersister        jdbcPersister;
    private final CSVReportPersister   csvReportPersister;
    private final DisplayConfiguration displayConfiguration;

    @Inject
    public ViewBuilder(JdbcDataPersisterFactory jdbcDataPersisterFactory,
                       CSVReportPersister csvReportPersister,
                       XmlConfigurationManager confManager,
                       Configurator configurator) throws IOException, URISyntaxException {

        Set<ViewConfiguration> views = new HashSet<>();
        readViewsFromConfiguration(confManager, configurator, views);
        readViewsFromResources(confManager, views);

        this.displayConfiguration = new DisplayConfiguration();
        this.displayConfiguration.setViews(new ArrayList<>(views));
        this.jdbcPersister = jdbcDataPersisterFactory.getDataPersister();
        this.csvReportPersister = csvReportPersister;
    }

    protected void readViewsFromResources(XmlConfigurationManager confManager, Set<ViewConfiguration> views) throws IOException {
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile()) {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith("views" + File.separator) && name.endsWith(".xml")) {
                    DisplayConfiguration dc = confManager.loadConfiguration(DisplayConfiguration.class, name);
                    views.addAll(dc.getViews());
                }
            }
            jar.close();
        }
    }

    protected void readViewsFromConfiguration(XmlConfigurationManager confManager,
                                              Configurator configurator,
                                              Set<ViewConfiguration> views) throws IOException {

        for (String view : configurator.getArray(VIEWS_CONFIGURATION)) {
            DisplayConfiguration dc = confManager.loadConfiguration(DisplayConfiguration.class, view);
            views.addAll(dc.getViews());
        }
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

    public ViewData getViewData(ValueData metricValue) {
        ViewData viewData = new ViewData(); // include title row

        List<ValueData> allMetricValues = treatAsList(metricValue);

        // return empty view data if there is empty metricValue
        if (allMetricValues.size() == 0) {
            return viewData;
        }

        SectionData sectionData = new SectionData();

        // add title row
        MapValueData firstRow = (MapValueData)allMetricValues.get(0);
        List<ValueData> titleRow = getRowKeys(firstRow);
        sectionData.add(titleRow);

        // transform MapValueData rows into the List<ValueData> rows
        for (ValueData row : allMetricValues) {
            sectionData.add(getRowValues((MapValueData)row));
        }

        viewData.put("section_expended", sectionData);

        return viewData;
    }

    private List<ValueData> getRowKeys(MapValueData mapRow) {
        List<ValueData> rowKeys = new ArrayList<>(mapRow.size());
        for (String key : mapRow.getAll().keySet()) {
            rowKeys.add(new StringValueData(key));
        }

        return rowKeys;
    }

    private List<ValueData> getRowValues(MapValueData mapRow) {
        List<ValueData> rowValues = new ArrayList<>(mapRow.size());
        for (Entry<String, ValueData> entry : mapRow.getAll().entrySet()) {
            rowValues.add(entry.getValue());
        }

        return rowValues;
    }

    private boolean isSimplified(Context context) {
        return !context.exists(Parameters.SORT)
               && !context.exists(Parameters.PAGE)
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
                if (viewConf.getTimeUnit() != null) {
                    for (String timeUnitParam : viewConf.getTimeUnit().split(",")) {
                        Context newContext = context.cloneAndPut(Parameters.TIME_UNIT, timeUnitParam.toUpperCase());
                        ComputeViewDataAction task = new ComputeViewDataAction(viewConf, newContext);
                        forkJoinPool.submit(task);

                        tasks.add(task);
                    }
                } else if (viewConf.getPassedDaysCount() != null) {
                    for (String passedDaysCountParam : viewConf.getPassedDaysCount().split(",")) {
                        Context newContext = context.cloneAndPut(Parameters.PASSED_DAYS_COUNT, passedDaysCountParam.toUpperCase());
                        ComputeViewDataAction task = new ComputeViewDataAction(viewConf, newContext);
                        forkJoinPool.submit(task);

                        tasks.add(task);
                    }
                } else {
                    ComputeViewDataAction task = new ComputeViewDataAction(viewConf, context);
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
    protected ViewData loadViewData(ViewConfiguration viewConf, Context context) throws IOException {
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
            this.context = initializeTimeInterval(context);
        }

        @Override
        protected void compute() {
            String viewId = getId(viewConf.getName(), context);

            try {
                ViewData viewData = doCompute();
                retainViewData(viewId, viewData, context);
            } catch (Exception e) {
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
                IllegalAccessException | InstantiationException | ParseException e) {
                throw new IOException(e);
            } finally {
                LOG.info("ViewBuilder is finished in " + (System.currentTimeMillis() - start) / 1000 + " sec. for " +
                         viewConf.getName() + " with context " + context);
            }
        }

        private Row getRow(RowConfiguration rowConf) throws NoSuchMethodException,
                                                            ClassNotFoundException,
                                                            InstantiationException,
                                                            IllegalAccessException,
                                                            InvocationTargetException {
            String className = rowConf.getClazz();
            if (context.exists(Parameters.IS_CSV_DATA) && DateRow.class.getName().equals(className)) {
                className = CVSDateRow.class.getName();
            }

            Constructor<?> constructor = Class.forName(className).getConstructor(Map.class);
            return (Row)constructor.newInstance(rowConf.getParamsAsMap());
        }
    }

    public Context initializeTimeInterval(Context context) throws ParseException {
        if (context.exists(Parameters.EXPANDED_METRIC_NAME)) {
            Metric expandableMetric = context.getExpandedMetric();
            if (expandableMetric instanceof AbstractTimelineProductUsageCondition) {
                return ((AbstractTimelineProductUsageCondition)expandableMetric).initContextBasedOnTimeInterval(context);
            } else if (expandableMetric instanceof AbstractTopEntitiesTime) {
                return ((AbstractTopEntitiesTime)expandableMetric).initContextBasedOnTimeInterval(context);
            }
        }

        Context.Builder builder = new Context.Builder(context);
        if (!builder.exists(Parameters.TO_DATE)) {
            builder.putDefaultValue(Parameters.TO_DATE);
        }
        builder.put(Parameters.REPORT_DATE, builder.getAsString(Parameters.TO_DATE));

        if (context.exists(Parameters.TIME_UNIT) && !context.exists(Parameters.IS_CUSTOM_DATE_RANGE)) {
            Parameters.TimeUnit timeUnit = builder.getTimeUnit();
            if (context.exists(Parameters.TIME_INTERVAL)) {
                int timeShift = (int)-context.getAsLong(Parameters.TIME_INTERVAL);
                return initDateInterval(builder.getAsDate(Parameters.TO_DATE), timeUnit, timeShift, builder);
            } else {
                return initDateInterval(builder.getAsDate(Parameters.TO_DATE), timeUnit, builder);
            }

        } else if (context.exists(Parameters.PASSED_DAYS_COUNT)) {
            return initDateInterval(builder.getAsDate(Parameters.TO_DATE), builder.getPassedDaysCount(), builder);

        } else {
            return builder.build();
        }
    }

    private int getRowCount(int rowCountFromConf, Context context) throws ParseException {
        if (context.exists(Parameters.TIME_UNIT) && context.getTimeUnit() == Parameters.TimeUnit.LIFETIME) {
            return 2;
        } else if (context.exists(Parameters.TIME_UNIT)
                   && context.exists(Parameters.IS_CUSTOM_DATE_RANGE)) {
            Calendar fromDate = context.getAsDate(Parameters.FROM_DATE);
            Calendar toDate = context.getAsDate(Parameters.TO_DATE);
            int rows = Utils.getUnitsAboveDates(context.getTimeUnit(), fromDate, toDate) + 1; // add one for metric name row

            return (rows > ViewBuilder.MAX_ROWS) ? ViewBuilder.MAX_ROWS : rows;
        } else {
            return rowCountFromConf;
        }
    }

    private String getId(String idFromConf, Context context) {
        String id = idFromConf;
        if (context.exists(Parameters.TIME_UNIT)) {
            id += "_" + context.getAsString(Parameters.TIME_UNIT).toLowerCase();
        } else if (context.exists(Parameters.PASSED_DAYS_COUNT)) {
            id += "_" + context.getPassedDaysCount().getFieldName();
        }

        return id;
    }

    /**
     * Returns list of view metrics which are expandable:
     * 1) reads view configuration
     * 2) gets list of view metric rows and their configurations;
     * makes sure the metric is expandable
     * 3) extracts metric type from configuration
     * 5) add this info into the List<Map<rowNumber, metricType>>
     */
    public List<Map<Integer, MetricType>> getViewExpandableMetricMap(String viewName) {
        List<Map<Integer, MetricType>> sectionList = new ArrayList<>();
        ViewConfiguration viewConf = displayConfiguration.getView(viewName);

        List<SectionConfiguration> sectionConfigurations = viewConf.getSections();
        for (int sectionNumber = 0; sectionNumber < sectionConfigurations.size(); sectionNumber++) {
            SectionConfiguration sectionConf = sectionConfigurations.get(sectionNumber);

            sectionList.add(new LinkedHashMap<Integer, MetricType>());
            List<RowConfiguration> rowConfigurations = sectionConf.getRows();
            for (int rowNumber = 0; rowNumber < rowConfigurations.size(); rowNumber++) {
                RowConfiguration rowConf = rowConfigurations.get(rowNumber);

                if (rowConf.getClazz().equals(MetricRow.class.getCanonicalName())) {  // check if this is metric row
                    String metricName = rowConf.getParamsAsMap().get("name").toUpperCase();
                    MetricType metricType = MetricType.valueOf(metricName);
                    Metric metric = MetricFactory.getMetric(metricType);

                    if (metric != null && (metric instanceof Expandable)) {
                        // add info about metric in to the sectionList = List<Map<rowNumber, metricType>>
                        sectionList.get(sectionNumber).put(rowNumber, metricType);
                    }
                }
            }
        }

        return sectionList;
    }
}
