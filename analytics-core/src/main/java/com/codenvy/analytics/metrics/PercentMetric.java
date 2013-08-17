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

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract class PercentMetric extends AbstractMetric {

    private final Metric totalMetric;
    private final Metric numberMetric;

    PercentMetric(MetricType metricType, Metric totalMetric, Metric particalMetric) {
        super(metricType);

        this.totalMetric = totalMetric;
        this.numberMetric = particalMetric;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public Set<MetricParameter> getParams() {
        Set<MetricParameter> params =
                (Set<MetricParameter>)((HashSet<MetricParameter>)numberMetric.getParams()).clone();
        params.addAll(totalMetric.getParams());

        return params;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        double total = totalMetric.getValue(context).getAsDouble();
        double number = numberMetric.getValue(context).getAsDouble();

        double percent = 100D * number / total;

        return new DoubleValueData(percent);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }
}
