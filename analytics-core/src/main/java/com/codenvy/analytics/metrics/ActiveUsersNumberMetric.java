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
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ActiveUsersNumberMetric extends CalculatedMetric {

    ActiveUsersNumberMetric() {
        super(MetricType.ACTIVE_USERS, MetricType.ACTIVE_USERS_SET);
    }

    /** {@inheritDoc} */
    @Override
    public Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of active users";
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        SetStringValueData valueData = (SetStringValueData) super.getValue(context);
        return new LongValueData(valueData.size());
    }
}
