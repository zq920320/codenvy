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

import com.codenvy.analytics.old_metrics.value.ListStringValueData;
import com.codenvy.analytics.old_metrics.value.StringValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersProfileEmailMetric extends CalculatedMetric {

    UsersProfileEmailMetric() {
        super(MetricType.USER_PROFILE_EMAIL, MetricType.USER_UPDATE_PROFILE);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ListStringValueData valueData = (ListStringValueData)super.getValue(context);
        return new StringValueData(((UserUpdateProfileMetric)basedMetric).getEmail(valueData));
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return StringValueData.class;
    }

    @Override
    public String getDescription() {
        return "The user's email";
    }
}
