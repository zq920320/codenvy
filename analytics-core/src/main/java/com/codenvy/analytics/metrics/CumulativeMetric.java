/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The value of the metric will be calculated as: previous value + added value - removed value.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class CumulativeMetric extends AbstractMetric {

    private final Metric                 addedMetric;
    private final Metric                 removedMetric;
    private final Map<String, ValueData> cachedValues;

    CumulativeMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) {
        super(metricType);

        this.addedMetric = addedMetric;
        this.removedMetric = removedMetric;
        this.cachedValues = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        Set<Parameters> params = addedMetric.getParams();
        params.addAll(removedMetric.getParams());
        params.remove(Parameters.FROM_DATE);

        return params;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws InitialValueNotFoundException, IOException {
        context = Utils.clone(context);
        Parameters.FROM_DATE.put(context, Parameters.TO_DATE.get(context));
        Parameters.TIME_UNIT.put(context, Parameters.TimeUnit.DAY.name());

        // TODO getAvailableFilters return ???

        try {
            InitialValueContainer.validateExistenceInitialValueBefore(context);
            ValueData valueData = doGetValue(context);

            putInCache(valueData, context);

            return valueData;
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private void putInCache(ValueData valueData, Map<String, String> context) throws ParseException {
        if (Utils.getToDate(context).get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cachedValues.put(Parameters.TO_DATE.get(context), valueData);
        }
    }

    private ValueData getFromCache(Map<String, String> context) {
        return cachedValues.get(Parameters.TO_DATE.get(context));
    }

    private ValueData doGetValue(Map<String, String> context) throws IOException, ParseException {
        ValueData valueData = getFromCache(context);
        if (valueData == null) {
            valueData = InitialValueContainer.getInitialValue(metricName, context);
        }

        if (valueData != null) {
            return valueData;
        }

        LongValueData addedEntity = (LongValueData)addedMetric.getValue(context);
        LongValueData removedEntity = (LongValueData)removedMetric.getValue(context);
        LongValueData previousEntity = (LongValueData)getValue(Utils.prevDateInterval(context));

        return new LongValueData(previousEntity.getAsLong() + addedEntity.getAsLong() - removedEntity.getAsLong());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
