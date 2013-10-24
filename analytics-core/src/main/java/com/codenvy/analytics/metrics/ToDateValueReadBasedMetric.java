/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.text.ParseException;
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

        try {
            Calendar lastDay = Utils.parseDate(Parameters.TO_DATE.getDefaultValue());
            Calendar toDate = Utils.getToDate(context);

            if (toDate.after(lastDay)) {
                Parameters.FROM_DATE.put(context, Parameters.TO_DATE.getDefaultValue());
                Parameters.TO_DATE.putDefaultValue(context);
            } else {
                Parameters.FROM_DATE.put(context, Parameters.TO_DATE.get(context));
            }

            return super.getValue(context);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new LinkedHashSet<>(
                Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData read(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        return FSValueDataManager.loadValue(metricType, uuid);
    }
}
