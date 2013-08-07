/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoginTotalMetric extends ListMetric {

    public UserLoginTotalMetric() {
        super(MetricType.USER_LOGIN_TOTAL, new Metric[]{MetricFactory.createMetric(MetricType.USER_LOGIN_GITHUB),
                                                        MetricFactory.createMetric(MetricType.USER_LOGIN_GOOGLE),
                                                        MetricFactory.createMetric(MetricType.USER_LOGIN_FORM)});
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
