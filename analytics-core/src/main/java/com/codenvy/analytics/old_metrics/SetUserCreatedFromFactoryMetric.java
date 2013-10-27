/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.SetStringValueData;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class SetUserCreatedFromFactoryMetric extends ValueReadBasedMetric {

    public SetUserCreatedFromFactoryMetric() {
        super(MetricType.SET_USER_CREATED_FROM_FACTORY);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return SetStringValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    @Override
    public String getDescription() {
        return "The list of users who used factory url and than created its own account";
    }
}
