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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.Parameters.PassedDaysCount;

/** @author Anatoliy Bazko */
public abstract class AbstractTopEntitiesTime extends CalculatedMetric {

    public static final String ENTITY = "entity";

    public static final int MAX_DOCUMENT_COUNT = 100;

    private final MetricFilter filterParameter;
    
    private final static List<PassedDaysCount> DAY_INTERVALS = Arrays.asList(new PassedDaysCount[]{
      PassedDaysCount.BY_1_DAY,
      PassedDaysCount.BY_7_DAYS,
      PassedDaysCount.BY_30_DAYS,
      PassedDaysCount.BY_60_DAYS,
      PassedDaysCount.BY_90_DAYS,
      PassedDaysCount.BY_365_DAYS,
      PassedDaysCount.BY_LIFETIME
    });

    public AbstractTopEntitiesTime(MetricType metricType, MetricType[] basedMetric, MetricFilter filterParameter) {
        super(metricType, basedMetric);
        this.filterParameter = filterParameter;
    }

    @Override
    public ValueData getValue(Context context) throws IOException {

        try {
            ListValueData top = getTopEntities(context);
            String[] filterValue = extractEntityNames(top);

            if (filterValue.length == 0) {
                return combineDefaultResult(top);
            }

            return combineResult(top, filterValue, context);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private ValueData combineDefaultResult(ListValueData top) {
        List<ValueData> result = new ArrayList<>(top.size());

        for (ValueData item : top.getAll()) {
            MapValueData next = (MapValueData)item;
            String paramName = filterParameter.name().toLowerCase();

            Map<String, ValueData> entity = next.getAll();
            String entityName = entity.get(paramName).getAsString();

            Map<String, ValueData> row = new HashMap<>(9);
            row.put(ENTITY, StringValueData.valueOf(entityName));
            row.put(SESSIONS, entity.get(SESSIONS));
            
            for (PassedDaysCount interval : DAY_INTERVALS) {
                ListValueData entities = ListValueData.DEFAULT;
                ValueData entityTimeValue = getEntityTimeValue(entities, entityName, TIME);
                String fieldName = interval.getFieldName();

                row.put(fieldName, entityTimeValue);
            }

            result.add(new MapValueData(row));
        }

        return new ListValueData(result);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    private ValueData combineResult(ListValueData top, String[] filterValue, Context context) throws ParseException, IOException {
        List<ValueData> result = new ArrayList<>(top.size());

        for (ValueData item : top.getAll()) {
            MapValueData next = (MapValueData)item;
            String paramName = filterParameter.name().toLowerCase();

            Map<String, ValueData> entity = next.getAll();
            String entityName = entity.get(paramName).getAsString();

            Map<String, ValueData> row = new HashMap<>(9);
            row.put(ENTITY, StringValueData.valueOf(entityName));
            row.put(SESSIONS, entity.get(SESSIONS));
            
            for (PassedDaysCount interval : DAY_INTERVALS) {
                ListValueData entities = getEntities(context, interval, filterValue);
                ValueData entityTimeValue = getEntityTimeValue(entities, entityName, TIME);
                String fieldName = interval.getFieldName();

                row.put(fieldName, entityTimeValue);
            }

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

    private String[] extractEntityNames(ListValueData top) {
        List<ValueData> items = top.getAll();
        String[] result = new String[items.size()];

        for (int i = 0; i < items.size(); i++) {
            MapValueData next = (MapValueData)items.get(i);
            String entityParam = filterParameter.name().toLowerCase();
            result[i] = next.getAll().get(entityParam).getAsString();
        }

        return result;
    }

    /** @return top entities for required period sorted by usage time */
    private ListValueData getTopEntities(Context context) throws ParseException, IOException {
        Context.Builder builder = new Context.Builder();
        builder.putAll(context);

        builder.put(Parameters.SORT, "-" + TIME);
        builder.put(Parameters.PAGE, 1);
        builder.put(Parameters.PER_PAGE, MAX_DOCUMENT_COUNT);

        return ValueDataUtil.getAsList(basedMetric[0], builder.build());
    }

    private ListValueData getEntities(Context context, PassedDaysCount passedDaysCount, String[] filterValue)
            throws ParseException, IOException {

        Context.Builder builder = initContextBuilder(context, passedDaysCount);
        builder.put(filterParameter, filterValue);

        return ValueDataUtil.getAsList(basedMetric[0], builder.build());
    }

    private Context.Builder initContextBuilder(Context basedContext, PassedDaysCount passedDaysCount) throws ParseException {
        Calendar toDate = basedContext.getAsDate(Parameters.TO_DATE);

        Context.Builder builder = new Context.Builder();
        builder.putAll(basedContext);

        if (passedDaysCount == PassedDaysCount.BY_LIFETIME) {
            builder.putDefaultValue(Parameters.FROM_DATE);
            
        } else {
            Calendar fromDate = (Calendar)toDate.clone();
            fromDate.add(Calendar.DAY_OF_MONTH, 1 - passedDaysCount.getDayCount());
            builder.put(Parameters.FROM_DATE, fromDate);
        }

        return builder;
    }
    
    /**
     * Return context with fixed time parameters
     */
    public Context initContextBasedOnTimeInterval(Context context) throws ParseException {
        if (context.exists(Parameters.TIME_INTERVAL)) {
            int timeInterval = (int)context.getAsLong(Parameters.TIME_INTERVAL);
            if (timeInterval < DAY_INTERVALS.size()) {
                context = initContextBuilder(context, DAY_INTERVALS.get(timeInterval)).build();
            }
        }

        return context;
    }
}
