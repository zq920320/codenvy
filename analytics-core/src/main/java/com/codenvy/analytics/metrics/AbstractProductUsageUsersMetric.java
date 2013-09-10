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


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract public class AbstractProductUsageUsersMetric extends CalculatedMetric {

    private final long minTime;

    private final long maxTime;

    public AbstractProductUsageUsersMetric(MetricType metricType, long minTime, long maxTime) {
        super(metricType, MetricType.PRODUCT_USAGE_SESSIONS);
        this.minTime = minTime;
        this.maxTime = maxTime;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    public ValueData getValue(Map<String, String> context) throws IOException {
        ListListStringValueData value = (ListListStringValueData)super.getValue(context);

        int count = 0;

        Map<String, Long> statistic = collectStatistic(value);
        for (Map.Entry<String, Long> entry : statistic.entrySet()) {
            long time = entry.getValue() / 60;
            if (minTime < time && time <= maxTime) {
                count++;
            }
        }

        return new LongValueData(count);
    }

    private Map<String, Long> collectStatistic(ListListStringValueData value) {
        Map<String, Long> results = new HashMap<>();

        ProductUsageSessionsMetric usageTimeMetric = (ProductUsageSessionsMetric)basedMetric;

        for (ListStringValueData item : value.getAll()) {
            String user = usageTimeMetric.getUser(item);

            if (Utils.isRegisteredUser(user)) {
                long time = usageTimeMetric.getTime(item);
                if (results.containsKey(user)) {
                    time += results.get(user);
                }

                results.put(user, time);
            }
        }

        return results;
    }
}
