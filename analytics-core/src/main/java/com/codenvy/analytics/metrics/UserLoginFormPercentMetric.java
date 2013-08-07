/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UserLoginFormPercentMetric extends PercentMetric {

    public UserLoginFormPercentMetric() {
        super(MetricType.USER_LOGIN_FORM_PERCENT,
              MetricFactory.createMetric(MetricType.USER_LOGIN_TOTAL),
              MetricFactory.createMetric(MetricType.USER_LOGIN_FORM));
    }
}
