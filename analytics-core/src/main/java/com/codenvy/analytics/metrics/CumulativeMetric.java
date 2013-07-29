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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * The value of the metric will be calculated as: previous value + added value - removed value.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class CumulativeMetric extends CalculateBasedMetric {

    private final InitialValueContainer iValueContainer;
    private final Metric                addedMetric;
    private final Metric                removedMetric;

    CumulativeMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) {
        super(metricType);

        this.iValueContainer = InitialValueContainer.getInstance();
        this.addedMetric = addedMetric;
        this.removedMetric = removedMetric;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<MetricParameter> getParams() {
        Set<MetricParameter> params = addedMetric.getParams();
        params.addAll(removedMetric.getParams());

        params.remove(MetricParameter.FROM_DATE);

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData evaluate(Map<String, String> context) throws InitialValueNotFoundException, IOException {
        context = Utils.clone(context);
        Utils.putFromDate(context, Utils.getToDate(context));
        Utils.putTimeUnit(context, TimeUnit.DAY);

        validateExistenceInitialValueBefore(context);

        try {
            return iValueContainer.getInitalValue(metricType, makeUUID(context).toString());
        } catch (InitialValueNotFoundException e) {
            // ignoring, may be next time lucky
        }

        LongValueData addedEntities = (LongValueData)addedMetric.getValue(context);
        LongValueData removedEntities = (LongValueData)removedMetric.getValue(context);

        context = Utils.prevDateInterval(context);

        LongValueData previousEntities = (LongValueData)getValue(context);

        return new LongValueData(previousEntities.getAsLong() + addedEntities.getAsLong() - removedEntities.getAsLong());
    }

    protected void validateExistenceInitialValueBefore(Map<String, String> context) throws InitialValueNotFoundException, IOException {
        iValueContainer.validateExistenceInitialValueBefore(metricType, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
