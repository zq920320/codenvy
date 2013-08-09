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
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersProfileMetric extends ValueReadBasedMetric {

    UsersProfileMetric() {
        super(MetricType.USER_PROFILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return evaluate(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                MetricParameter.ALIAS}));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    protected String getEmail(ListListStringValueData data) {
        return getItem(data, 0);
    }

    protected String getFirstName(ListListStringValueData data) {
        return getItem(data, 1);
    }

    protected String getLastName(ListListStringValueData data) {
        return getItem(data, 2);
    }

    protected String getCompany(ListListStringValueData data) {
        return getItem(data, 3);
    }

    protected String getPhone(ListListStringValueData data) {
        return getItem(data, 4);
    }

    protected String getJob(ListListStringValueData data) {
        return getItem(data, 5);
    }

    private String getItem(ListListStringValueData data, int index) {
        return data.getAll().get(0).getAll().get(index);
    }
}
