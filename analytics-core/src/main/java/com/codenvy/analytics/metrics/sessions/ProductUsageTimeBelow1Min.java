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
public class ProductUsageTimeBelow1Min extends CalculatedMetric {

    public ProductUsageTimeBelow1Min() {
        super(MetricType.PRODUCT_USAGE_TIME_BELOW_1_MIN,
              new MetricType[]{MetricType.PRODUCT_USAGE_SESSIONS_BELOW_1_MIN});
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        LongValueData value = (LongValueData)basedMetric[0].getValue(context);
        return LongValueData.valueOf(value.getAsLong() * 60 * 1000);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The total time of all sessions in persistent workspaces with duration less or equals to 1 minute";
    }
}
