/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoginTotalMetric extends ListMetric {

    public UserLoginTotalMetric() {
        super(MetricType.USER_LOGIN_TOTAL, new MetricType[]{MetricType.USER_LOGIN_GITHUB,
                                                            MetricType.USER_LOGIN_GOOGLE,
                                                            MetricType.USER_LOGIN_FORM});
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The total number of logged in actions";
    }
}
