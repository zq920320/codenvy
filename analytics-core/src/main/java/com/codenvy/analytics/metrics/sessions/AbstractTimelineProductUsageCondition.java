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
public abstract class AbstractTimelineProductUsageCondition extends CalculatedMetric {

    public static final String BY_1_DAY    = "by_1_day";
    public static final String BY_7_DAYS   = "by_7_days";
    public static final String BY_30_DAYS  = "by_30_days";
    public static final String BY_60_DAYS  = "by_60_days";
    public static final String BY_90_DAYS  = "by_90_days";
    public static final String BY_365_DAYS = "by_365_days";

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
            LongValueData by1Day = getNumberOfUsers(context, 1);
            LongValueData by7Day = getNumberOfUsers(context, 7);
            LongValueData by30Day = getNumberOfUsers(context, 30);
            LongValueData by60Day = getNumberOfUsers(context, 60);
            LongValueData by90Day = getNumberOfUsers(context, 90);
            LongValueData by365Day = getNumberOfUsers(context, 365);

            Map<String, ValueData> row = new HashMap<>(6);
            row.put(BY_1_DAY, by1Day);
            row.put(BY_7_DAYS, by7Day);
            row.put(BY_30_DAYS, by30Day);
            row.put(BY_60_DAYS, by60Day);
            row.put(BY_90_DAYS, by90Day);
            row.put(BY_365_DAYS, by365Day);

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

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
