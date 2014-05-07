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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author Anatoliy Bazko */
public abstract class AbstractTimelineProductUsageCondition extends CalculatedMetric implements Expandable {

    private enum DaysInterval {
        BY_1_DAY("by_1_day", 1),
        BY_7_DAYS("by_7_days", 7),
        BY_30_DAYS("by_30_days", 30),
        BY_60_DAYS("by_60_days", 60),
        BY_90_DAYS("by_90_days", 90),
        BY_365_DAYS("by_365_days", 365);
        
        String fieldName;
        int dayCount;
        
        DaysInterval(String fieldName, int dayCount) {
            this.fieldName = fieldName;
            this.dayCount = dayCount;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public int getDayCount() {
            return dayCount;
        }      
    }

    protected AbstractTimelineProductUsageCondition(MetricType metricType,
                                                    MetricType[] basedMetricTypes) {
        super(metricType, basedMetricTypes);
    }

    protected AbstractTimelineProductUsageCondition(MetricType metricType,
                                                    Metric[] basedMetric) {
        super(metricType, basedMetric);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        try {
            Map<String, ValueData> row = new HashMap<>(DaysInterval.values().length);
            
            for (DaysInterval interval: DaysInterval.values()) {
                LongValueData numberOfUsers = getNumberOfUsers(context, interval.getDayCount());
                String fieldName = interval.getFieldName();
                
                row.put(fieldName, numberOfUsers);
            }

            List<ValueData> result = new ArrayList<>();
            result.add(new MapValueData(row));

            return new ListValueData(result);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private LongValueData getNumberOfUsers(Context context, int dayCount) throws ParseException, IOException {
        context = initContext(context, dayCount);
        return (LongValueData)basedMetric[0].getValue(context);
    }

    public Context initContext(Context basedContext, int dayCount) throws ParseException {
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
            int timeInterval = (int) context.getAsLong(Parameters.TIME_INTERVAL);
            if (timeInterval < DaysInterval.values().length) {            
                int dayCount = DaysInterval.values()[timeInterval].getDayCount();
                context = initContext(context, dayCount);
            }
        }
        
        return context;
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
    
    @Override
    public String getExpandedValueField() {
        return USER;
    }
    
    @Override
    public ListValueData getExpandedValue(Context context) throws IOException {
        return ((Expandable) basedMetric[0]).getExpandedValue(context);
    }
}
