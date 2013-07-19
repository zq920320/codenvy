/*
 *    Copyright (C) 2013 Codenvy.
 *
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
public class UsersProfileMetric extends ReadBasedMetric {

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
        return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
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
