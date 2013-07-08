/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
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

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return evaluate(context);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{
                MetricParameter.ALIAS}));
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return ListListStringValueData.class;
    }

    protected String getEmail(Map<String, String> context) throws IOException {
        return getItem(context, 0);
    }

    protected String getFirstName(Map<String, String> context) throws IOException {
        return getItem(context, 1);
    }

    protected String getLastName(Map<String, String> context) throws IOException {
        return getItem(context, 2);
    }

    protected String getCompany(Map<String, String> context) throws IOException {
        return getItem(context, 3);
    }

    protected String getPhone(Map<String, String> context) throws IOException {
        return getItem(context, 4);
    }

    private String getItem(Map<String, String> context, int index) throws IOException  {
        ListListStringValueData data = (ListListStringValueData)getValue(context);
        return data.getAll().get(0).getAll().get(index);
    }
}
