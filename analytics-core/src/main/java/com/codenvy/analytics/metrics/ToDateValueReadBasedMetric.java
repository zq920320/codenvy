/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class ToDateValueReadBasedMetric extends ReadBasedMetric {

    public ToDateValueReadBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        context = Utils.clone(context);

        Calendar lastDay = Utils.parseDate(MetricParameter.TO_DATE.getDefaultValue());
        Calendar toDate = Utils.getToDate(context);

        if (toDate.after(lastDay)) {
            Utils.putFromDate(context, MetricParameter.TO_DATE.getDefaultValue());
            Utils.putToDate(context, MetricParameter.TO_DATE.getDefaultValue());
        } else {
            Utils.putFromDate(context, Utils.getToDateParam(context));
        }

        return super.getValue(context);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE, MetricParameter.TO_DATE}));
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData read(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        return FSValueDataManager.loadValue(metricType, uuid);
    }
}
