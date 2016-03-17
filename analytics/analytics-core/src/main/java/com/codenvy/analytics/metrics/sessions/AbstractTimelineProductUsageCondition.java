/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.Parameters.PassedDaysCount;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author Anatoliy Bazko */
public abstract class AbstractTimelineProductUsageCondition extends CalculatedMetric implements Expandable {

    private final static List<PassedDaysCount> DAY_INTERVALS = Arrays.asList(new PassedDaysCount[]{
       PassedDaysCount.BY_1_DAY,
       PassedDaysCount.BY_7_DAYS,
       PassedDaysCount.BY_30_DAYS,
       PassedDaysCount.BY_60_DAYS,
       PassedDaysCount.BY_90_DAYS,
       PassedDaysCount.BY_365_DAYS,
     });    
    
    protected AbstractTimelineProductUsageCondition(MetricType metricType, MetricType[] basedMetricTypes) {
        super(metricType, basedMetricTypes);
    }

    protected AbstractTimelineProductUsageCondition(MetricType metricType, Metric[] basedMetric) {
        super(metricType, basedMetric);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        try {
            Map<String, ValueData> row = new HashMap<>(DAY_INTERVALS.size());

            for (PassedDaysCount interval : DAY_INTERVALS) {
                LongValueData numberOfUsers = getNumberOfUsers(context, interval.getDayCount());
                String fieldName = interval.getFieldName();

                row.put(fieldName, numberOfUsers);
            }

            List<ValueData> result = Arrays.asList(new ValueData[]{MapValueData.valueOf(row)});
            return ListValueData.valueOf(result);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private LongValueData getNumberOfUsers(Context context, int dayCount) throws ParseException, IOException {
        context = initContext(context, dayCount);
        return (LongValueData)basedMetric[0].getValue(context);
    }

    private Context initContext(Context basedContext, int dayCount) throws ParseException {
        Context.Builder builder = new Context.Builder(basedContext);

        builder.putDefaultValue(Parameters.FROM_DATE);

        Calendar toDate = builder.getAsDate(Parameters.TO_DATE);
        Calendar fromDate = builder.getAsDate(Parameters.FROM_DATE);

        toDate.add(Calendar.DAY_OF_MONTH, 1 - dayCount);
        if (fromDate.after(toDate)) {
            builder.put(Parameters.TO_DATE, fromDate);
        } else {
            builder.put(Parameters.TO_DATE, toDate);
        }

        return builder.build();
    }

    /**
     * Return context with fixed
     */
    public Context initContextBasedOnTimeInterval(Context context) throws ParseException {
        if (context.exists(Parameters.TIME_INTERVAL)) {
            int timeInterval = (int)context.getAsLong(Parameters.TIME_INTERVAL);
            if (timeInterval < DAY_INTERVALS.size()) {
                context = initContext(context, DAY_INTERVALS.get(timeInterval).getDayCount());
            }
        }

        return context;
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        return ((Expandable)basedMetric[0]).getExpandedValue(context);
    }

    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[0]).getExpandedField();
    }
}
