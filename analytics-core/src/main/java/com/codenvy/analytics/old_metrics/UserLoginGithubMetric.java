/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.LongValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoginGithubMetric extends CalculatedMetric {

    public UserLoginGithubMetric() {
        super(MetricType.USER_LOGIN_GITHUB, MetricType.USER_SSO_LOGGED_IN);
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        Parameters.PARAM.put(context, "github");
        return super.getValue(context);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of authentication with GitHub account";
    }
}
