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

import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class CalculatedMetric extends AbstractMetric {

    public static final int TOP = 100;
    protected final Metric basedMetric;

    /**
     * {@link CalculatedMetric} constructor.
     *
     * @param metricType the current metric
     * @param basedMetric the metric on which current one depends on
     */
    CalculatedMetric(MetricType metricType, MetricType basedMetric) {
        super(metricType);
        this.basedMetric = MetricFactory.createMetric(basedMetric);
    }

    /** {@inheritedDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return basedMetric.getValue(context);
    }

    /** {@inheritedDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }
}
