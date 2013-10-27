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


package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
abstract class ListMetric extends AbstractMetric {

    private final Metric[] metrics;

    ListMetric(MetricType metricType, MetricType[] basedMetricTypes) {
        super(metricType);

        this.metrics = new Metric[basedMetricTypes.length];
        for (int i = 0; i < basedMetricTypes.length; i++) {
            this.metrics[i] = MetricFactory.createMetric(basedMetricTypes[i]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        Set<Parameters> params = new HashSet<>();
        for (Metric metric : metrics) {
            params.addAll(metric.getParams());
        }

        return params;
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        ValueData valueData = metrics[0].getValue(context);
        for (int i = 1; i < metrics.length; i++) {
            valueData = valueData.union(metrics[i].getValue(context));
        }

        return valueData;
    }
}
