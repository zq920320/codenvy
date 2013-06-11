/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersProfileCompanyMetric extends CalculateBasedMetric {

    private final UsersProfileMetric basedMetric;
    
    UsersProfileCompanyMetric() {
        super(MetricType.USER_PROFILE_COMPANY);
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
        return new StringValueData(basedMetric.getCompany(context));
    }

    /** {@inheritDoc} */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return StringValueData.class;
    }
}
