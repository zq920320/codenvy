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

package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.old_metrics.value.LongValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class InactiveWsMetric extends CalculatedMetric {

    InactiveWsMetric() {
        super(MetricType.INACTIVE_WS, MetricType.TOTAL_WORKSPACES);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of inactive workspaces";
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        LongValueData totalWs = (LongValueData)super.getValue(context);
        LongValueData activeWs =
                (LongValueData)MetricFactory.createMetric(MetricType.ACTIVE_WS).getValue(context);

        return new LongValueData(totalWs.getAsLong() - activeWs.getAsLong());
    }
}
