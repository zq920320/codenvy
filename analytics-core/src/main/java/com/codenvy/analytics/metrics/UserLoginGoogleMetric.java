/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoginGoogleMetric extends CalculatedMetric {

    public UserLoginGoogleMetric() {
        super(MetricType.USER_LOGIN_GOOGLE, MetricType.USER_SSO_LOGGED_IN);
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        Utils.putParam(context, "google");
        return super.getValue(context);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
