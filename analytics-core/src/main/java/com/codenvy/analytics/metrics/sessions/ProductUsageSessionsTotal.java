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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageSessionsTotal extends CalculatedMetric {

    public ProductUsageSessionsTotal() {
        super(MetricType.PRODUCT_USAGE_SESSIONS_TOTAL,
              new MetricType[]{MetricType.PRODUCT_USAGE_SESSIONS_BELOW_1_MIN,
                               MetricType.PRODUCT_USAGE_SESSIONS_BETWEEN_1_AND_10_MIN,
                               MetricType.PRODUCT_USAGE_SESSIONS_BETWEEN_10_AND_60_MIN,
                               MetricType.PRODUCT_USAGE_SESSIONS_ABOVE_60_MIN});
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        LongValueData value1 = (LongValueData)basedMetric[0].getValue(context);
        LongValueData value2 = (LongValueData)basedMetric[1].getValue(context);
        LongValueData value3 = (LongValueData)basedMetric[2].getValue(context);
        LongValueData value4 = (LongValueData)basedMetric[3].getValue(context);

        return new LongValueData(value1.getAsLong() + value2.getAsLong() + value3.getAsLong() + value4.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of all sessions in persistent workspaces";
    }
}
