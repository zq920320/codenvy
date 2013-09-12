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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageUsers010Metric extends CalculatedMetric {

    public ProductUsageUsers010Metric() {
        super(MetricType.PRODUCT_USAGE_USERS_0_10, MetricType.ACTIVE_USERS);
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ValueData value = super.getValue(context);
        ValueData value1 = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_USERS_10_60).getValue(context);
        ValueData value2 = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_USERS_60_300).getValue(context);
        ValueData value3 = MetricFactory.createMetric(MetricType.PRODUCT_USAGE_USERS_300_MORE).getValue(context);

        return new LongValueData(value.getAsLong() - value1.getAsLong() - value2.getAsLong() - value3.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of registered users who spent in product less than 10 minutes";
    }
}
