/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserAddedToWsInviteMetric extends CalculatedMetric {

    public UserAddedToWsInviteMetric() {
        super(MetricType.USER_ADDED_TO_WORKSPACE_INVITE, MetricType.USER_ADDED_TO_WORKSPACE);
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        MetricParameter.PARAM.put(context, "invite");
        return super.getValue(context);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of users who where added to workspace via invitation";
    }
}
