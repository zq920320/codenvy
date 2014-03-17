/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author Anatoliy Bazko */
public abstract class AbstractTopEntitiesTime extends CalculatedMetric {

    public static final String ENTITY      = "entity";
    public static final String BY_1_DAY    = "by_1_day";
    public static final String BY_7_DAY    = "by_7_days";
    public static final String BY_30_DAY   = "by_30_days";
    public static final String BY_60_DAY   = "by_60_days";
    public static final String BY_90_DAY   = "by_90_days";
    public static final String BY_365_DAY  = "by_365_days";
    public static final String BY_LIFETIME = "by_lifetime";

    public static final int MAX_DOCUMENT_COUNT = 100;
    public static final int LIFE_TIME_PERIOD   = -1;

    private final int          dayCount;
    private final MetricFilter filterParameter;

    public AbstractTopEntitiesTime(MetricType metricType,
                                   MetricType[] basedMetricTypes,
                                   MetricFilter filterParameter,
                                   int dayCount) {
        super(metricType, basedMetricTypes);
        this.dayCount = dayCount;
        this.filterParameter = filterParameter;
    }

    public AbstractTopEntitiesTime(MetricType metricType,
                                   Metric[] basedMetric,
                                   MetricFilter filterParameter,
                                   int dayCount) {
        super(metricType, basedMetric);
        this.dayCount = dayCount;
        this.filterParameter = filterParameter;
    }

    @Override
    public ValueData getValue(Context context) throws IOException {

        try {
            ListValueData top = getTopEntities(context, dayCount);
            String filterValue = extractEntityNames(top);

            if (filterValue.isEmpty()) {
                return combineResult(top,
                                     ListValueData.DEFAULT,
                                     ListValueData.DEFAULT,
                                     ListValueData.DEFAULT,
                                     ListValueData.DEFAULT,
                                     ListValueData.DEFAULT,
                                     ListValueData.DEFAULT,
                                     ListValueData.DEFAULT);
            }

            ListValueData by1Day = getEntities(context, 1, filterValue);
            ListValueData by7Day = getEntities(context, 7, filterValue);
            ListValueData by30Day = getEntities(context, 30, filterValue);
            ListValueData by60Day = getEntities(context, 60, filterValue);
            ListValueData by90Day = getEntities(context, 90, filterValue);
            ListValueData by365Day = getEntities(context, 365, filterValue);
            ListValueData byLifetime = getEntities(context, LIFE_TIME_PERIOD, filterValue);

            return combineResult(top,
                                 by1Day,
                                 by7Day,
                                 by30Day,
                                 by60Day,
                                 by90Day,
                                 by365Day,
                                 byLifetime);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    private ValueData combineResult(ListValueData top, ListValueData by1Day, ListValueData by7Day,
                                    ListValueData by30Day, ListValueData by60Day, ListValueData by90Day,
                                    ListValueData by365Day, ListValueData byLifetime) {

        List<ValueData> result = new ArrayList<>(top.size());

        for (ValueData item : top.getAll()) {
            MapValueData next = (MapValueData)item;
            String paramName = filterParameter.name().toLowerCase();

            Map<String, ValueData> entity = next.getAll();
            String entityName = entity.get(paramName).getAsString();

            Map<String, ValueData> row = new HashMap<>(9);
            row.put(ENTITY, StringValueData.valueOf(entityName));
            row.put(SESSIONS, entity.get(SESSIONS));
            row.put(BY_1_DAY, getEntityTimeValue(by1Day, entityName, TIME));
            row.put(BY_7_DAY, getEntityTimeValue(by7Day, entityName, TIME));
            row.put(BY_30_DAY, getEntityTimeValue(by30Day, entityName, TIME));
            row.put(BY_60_DAY, getEntityTimeValue(by60Day, entityName, TIME));
            row.put(BY_90_DAY, getEntityTimeValue(by90Day, entityName, TIME));
            row.put(BY_365_DAY, getEntityTimeValue(by365Day, entityName, TIME));
            row.put(BY_LIFETIME, getEntityTimeValue(byLifetime, entityName, TIME));

            result.add(new MapValueData(row));
        }

        return new ListValueData(result);
    }

    private ValueData getEntityTimeValue(ListValueData valueData, String entityName, String valueParam) {
        for (ValueData item : valueData.getAll()) {
            MapValueData next = (MapValueData)item;
            String entityParam = filterParameter.name().toLowerCase();

            Map<String, ValueData> entity = next.getAll();
            if (entityName.equals(entity.get(entityParam).getAsString())) {
                return entity.get(valueParam);
            }
        }

        return LongValueData.DEFAULT;
    }

    private String extractEntityNames(ListValueData top) {
        StringBuilder filterValue = new StringBuilder();

        for (ValueData item : top.getAll()) {
            MapValueData next = (MapValueData)item;
            String entityParam = filterParameter.name().toLowerCase();
            String entityName = next.getAll().get(entityParam).getAsString();

            if (filterValue.length() != 0) {
                filterValue.append(",");
            }

            filterValue.append(entityName);
        }

        return filterValue.toString();
    }

    /** @return top entities for required period sorted by usage time */
    private ListValueData getTopEntities(Context context, int dayCount) throws ParseException, IOException {
        Context.Builder builder = initContextBuilder(context, dayCount);
        builder.put(Parameters.SORT, "-" + TIME);
        builder.put(Parameters.PAGE, 1);
        builder.put(Parameters.PER_PAGE, MAX_DOCUMENT_COUNT);

        return ValueDataUtil.getAsList(basedMetric[0], builder.build());
    }

    private ListValueData getEntities(Context context, int dayCount, String filterValue)
            throws ParseException, IOException {

        Context.Builder builder = initContextBuilder(context, dayCount);
        builder.put(filterParameter, filterValue);

        return ValueDataUtil.getAsList(basedMetric[0], builder.build());
    }

    private Context.Builder initContextBuilder(Context basedContext, int dayCount) throws ParseException {
        Calendar toDate = basedContext.getAsDate(Parameters.TO_DATE);

        Context.Builder builder = new Context.Builder();
        builder.putAll(basedContext);

        if (dayCount == LIFE_TIME_PERIOD) {
            builder.putDefaultValue(Parameters.FROM_DATE);
        } else {
            Calendar fromDate = (Calendar)toDate.clone();
            fromDate.add(Calendar.DAY_OF_MONTH, 1 - dayCount);
            builder.put(Parameters.FROM_DATE, fromDate);
        }

        return builder;
    }
}
