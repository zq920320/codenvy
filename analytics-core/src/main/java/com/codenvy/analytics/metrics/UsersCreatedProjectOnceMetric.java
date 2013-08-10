/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersCreatedProjectOnceMetric extends CalculatedMetric {

    UsersCreatedProjectOnceMetric() {
        super(MetricType.USERS_CREATED_PROJECT_ONCE, MetricType.PROJECT_CREATED_USER_ACTIVE);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        SetStringValueData valueData = (SetStringValueData)super.getValue(context);
        return new LongValueData(valueData.size());
    }
}
