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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class NonActiveUsers extends CalculatedMetric {

    public NonActiveUsers() {
        super(MetricType.NON_ACTIVE_USERS, new MetricType[]{MetricType.ACTIVE_USERS,
                                                            MetricType.TOTAL_USERS});
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        LongValueData active = (LongValueData)basedMetric[0].getValue(context);
        LongValueData total = (LongValueData)basedMetric[1].getValue(context);

        return new LongValueData(total.getAsLong() - active.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "Non-active users";
    }
}
