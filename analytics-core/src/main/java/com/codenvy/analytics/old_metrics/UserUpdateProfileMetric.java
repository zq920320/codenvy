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

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.ListStringValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserUpdateProfileMetric extends ValueReadBasedMetric {

    UserUpdateProfileMetric() {
        super(MetricType.USER_UPDATE_PROFILE);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return evaluate(context);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>();
    }

    @Override
    public String getDescription() {
        return "User's profile";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListStringValueData.class;
    }

    public String getEmail(ListStringValueData data) {
        return getItem(data, 0);
    }

    public String getFirstName(ListStringValueData data) {
        return getItem(data, 1);
    }

    public String getLastName(ListStringValueData data) {
        return getItem(data, 2);
    }

    public String getCompany(ListStringValueData data) {
        return getItem(data, 3);
    }

    public String getPhone(ListStringValueData data) {
        return getItem(data, 4);
    }

    private String getItem(ListStringValueData data, int index) {
        return data.getAll().get(index);
    }
}
