/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersProfilePhoneMetric extends CalculateBasedMetric {

    private final UsersProfileMetric basedMetric;

    UsersProfilePhoneMetric() {
        super(MetricType.USER_PROFILE_PHONE);
        this.basedMetric = (UsersProfileMetric)MetricFactory.createMetric(MetricType.USER_PROFILE);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData) basedMetric.getValue(context);
        return new StringValueData(basedMetric.getPhone(valueData));
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return StringValueData.class;
    }
}
