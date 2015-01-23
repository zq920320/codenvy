/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class CalculatedMetric extends AbstractMetric {

    protected final Metric[] basedMetric;

    protected CalculatedMetric(MetricType metricType, MetricType[] basedMetricTypes) {
        super(metricType);

        this.basedMetric = new Metric[basedMetricTypes.length];
        for (int i = 0; i < basedMetricTypes.length; i++) {
            this.basedMetric[i] = MetricFactory.getMetric(basedMetricTypes[i]);
        }
    }

    /**
     * {@link CalculatedMetric} constructor.
     *
     * @param metricName
     *         the metric name, must be unique
     * @param basedMetricsNames
     *         the metrics names which is used to calculate the value of the metric. All metrics will be available
     *         inside {@link #getValue(Context)} method through array {@link #basedMetric} in the same order as were
     *         passed into constructor
     */
    public CalculatedMetric(String metricName, String[] basedMetricsNames) {
        super(metricName);

        this.basedMetric = new Metric[basedMetricsNames.length];
        for (int i = 0; i < basedMetricsNames.length; i++) {
            this.basedMetric[i] = MetricFactory.getMetric(basedMetricsNames[i]);
        }
    }

    protected CalculatedMetric(MetricType metricType, Metric[] basedMetric) {
        this(metricType.name(), basedMetric);
    }

    public CalculatedMetric(String metricType, Metric[] basedMetric) {
        super(metricType);
        this.basedMetric = basedMetric;
    }

}
