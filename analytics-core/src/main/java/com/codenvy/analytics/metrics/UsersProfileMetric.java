/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
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
public class UsersProfileMetric extends CalculateBasedMetric {

    UsersProfileMetric() {
        super(MetricType.USER_PROFILE);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<MetricParameter>(Arrays.asList(new MetricParameter[]{
                MetricParameter.ALIAS}));
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        return FSValueDataManager.load(metricType, makeUUID(context));
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return ListStringValueData.class;
    }

    protected String getEmail(Map<String, String> context) throws IOException {
        ListStringValueData data = (ListStringValueData)evaluate(context);
        return data.getAll().get(0);
    }

    protected String getFirstName(Map<String, String> context) throws IOException {
        ListStringValueData data = (ListStringValueData)evaluate(context);
        return data.getAll().get(1);
    }

    protected String getLastName(Map<String, String> context) throws IOException {
        ListStringValueData data = (ListStringValueData)evaluate(context);
        return data.getAll().get(2);
    }

    protected String getCompany(Map<String, String> context) throws IOException {
        ListStringValueData data = (ListStringValueData)evaluate(context);
        return data.getAll().get(3);
    }
}
